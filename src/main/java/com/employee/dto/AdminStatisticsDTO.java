package com.employee.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatisticsDTO {
    private Double totalHoursLogged;
    private Integer totalUsers;
    private Integer totalTimeEntries;
    private Integer totalMeetings;
    private Map<String, Double> hoursPerUser;
    private Map<LocalDate, Double> hoursPerDay;
}