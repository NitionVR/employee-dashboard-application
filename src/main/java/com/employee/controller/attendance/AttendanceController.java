package com.employee.controller.attendance;

import com.employee.dto.attendance.AttendanceRecordDTO;
import com.employee.dto.attendance.AttendanceStatusDTO;
import com.employee.dto.checkin.CheckInRequest;
import com.employee.dto.checkin.CheckOutRequest;
import com.employee.service.attendance.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceRecordDTO> checkIn(
            @RequestBody CheckInRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(attendanceService.checkIn(principal.getName(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AttendanceRecordDTO>> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal
    ) {
        return ResponseEntity.ok(attendanceService.getHistory(
                principal.getName(), startDate, endDate));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceRecordDTO>> getAllAttendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(attendanceService.getAllAttendanceRecords(startDate, endDate));
    }

    @GetMapping("/admin/office/{officeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AttendanceRecordDTO>> getAttendanceByOffice(
            @PathVariable Long officeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(attendanceService.getAttendanceByOffice(
                officeId, startDate, endDate));
    }

    @PostMapping("/check-out")
    public ResponseEntity<AttendanceRecordDTO> checkOut(
            @RequestBody CheckOutRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(attendanceService.checkOut(
                principal.getName(), request));
    }

    @GetMapping("/status")
    public ResponseEntity<AttendanceStatusDTO> getCurrentStatus(Principal principal) {
        return ResponseEntity.ok(attendanceService.getCurrentStatus(
                principal.getName()));
    }

    // Add endpoint for force check-out (admin only)
    @PostMapping("/admin/force-checkout/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AttendanceRecordDTO> forceCheckOut(
            @PathVariable Long userId,
            @RequestBody CheckOutRequest request
    ) {
        return ResponseEntity.ok(attendanceService.forceCheckOut(userId, request));
    }
}