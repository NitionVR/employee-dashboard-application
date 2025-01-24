package com.employee.controller.attendance;

import com.employee.dto.DepartmentAttendanceStatsDTO;
import com.employee.dto.user.UserAttendanceStatsDTO;
import com.employee.service.attendance.AttendanceStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance/stats")
@RequiredArgsConstructor
public class AttendanceStatisticsController {
    private final AttendanceStatisticsService statisticsService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserAttendanceStatsDTO> getUserStats(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(statisticsService.getUserStats(userId, startDate, endDate));
    }

    @GetMapping("/department")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentAttendanceStatsDTO> getDepartmentStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(statisticsService.getDepartmentStats(startDate, endDate));
    }
}