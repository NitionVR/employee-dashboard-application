package com.employee.dto.timesheet;

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
public class TimeEntryDTO {
    private Long id;
    private LocalDate date;
    private Double hours;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
