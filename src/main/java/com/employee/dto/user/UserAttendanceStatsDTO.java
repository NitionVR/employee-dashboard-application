package com.employee.dto.user;

import com.employee.dto.DailyStatsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAttendanceStatsDTO {
    private Long userId;
    private String userName;
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalDays;
    private long presentDays;
    private double totalAttendanceHours;
    private double totalTrackedHours;
    private LocalTime averageCheckInTime;
    private LocalTime averageCheckOutTime;
    private double averageWorkHours;
    private int lateCheckIns;
    private int earlyCheckOuts;
    private List<DailyStatsDTO> dailyStats;
}