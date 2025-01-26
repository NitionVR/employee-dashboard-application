package com.employee.dto;

import com.employee.dto.user.UserAttendanceStatsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentAttendanceStatsDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalEmployees;
    private double averageAttendance;
    private double averageWorkHours;
    private List<UserAttendanceStatsDTO> userStats;
}