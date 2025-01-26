package com.employee.dto.user;


import com.employee.dto.meeting.MeetingDTO;
import com.employee.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDTO {
    private UserDTO user;
    private Double totalHoursLogged;
    private Integer totalTimeEntries;
    private Integer totalMeetings;
    private Map<LocalDate, Double> hoursPerDay;
    private List<MeetingDTO> upcomingMeetings;
    private Double averageHoursPerDay;
}