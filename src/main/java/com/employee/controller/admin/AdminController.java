package com.employee.controller.admin;

import com.employee.dto.*;
import com.employee.dto.auth.UpdateRoleRequest;
import com.employee.dto.meeting.MeetingDTO;
import com.employee.dto.user.UserDTO;
import com.employee.dto.user.UserStatisticsDTO;
import com.employee.service.ExportService;
import com.employee.service.MeetingService;
import com.employee.service.TimeEntryService;
import com.employee.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")  // Ensures only admins can access these endpoints
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final TimeEntryService timeEntryService;
    private final MeetingService meetingService;
    private final ExportService exportService;

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserDTO> updateUserRole(
            @PathVariable Long userId,
            @RequestBody UpdateRoleRequest request
    ) {
        return ResponseEntity.ok(userService.updateUserRole(userId, request.getRole()));
    }

    @GetMapping("/statistics")
    public ResponseEntity<AdminStatisticsDTO> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(timeEntryService.getAdminStatistics(startDate, endDate));
    }

    @GetMapping("/users/{userId}/statistics")
    public ResponseEntity<UserStatisticsDTO> getUserStatistics(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(timeEntryService.getUserStatistics(userId, startDate, endDate));
    }

    @GetMapping("/meetings")
    public ResponseEntity<List<MeetingDTO>> getAllMeetings(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(meetingService.getAllMeetings(startDate, endDate));
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportData(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws IOException {
        byte[] report = exportService.generateReport(startDate, endDate);
        ByteArrayResource resource = new ByteArrayResource(report);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=employee_report.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
