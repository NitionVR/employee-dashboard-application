package com.employee.dto.attendance;


import com.employee.model.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecordDTO {
    private Long id;
    private String userName;
    private String userEmail;
    private String officeName;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private AttendanceStatus status;
    private String checkInLocation;
    private String checkOutLocation;
    private Double totalHours;
}