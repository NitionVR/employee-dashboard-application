package com.employee.dto;

import com.employee.model.attendance.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsDTO {
    private LocalDate date;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private double attendanceHours;
    private double trackedHours;
    private AttendanceStatus status;
    private boolean isLateCheckIn;
    private boolean isEarlyCheckOut;
}