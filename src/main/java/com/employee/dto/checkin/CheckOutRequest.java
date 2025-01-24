package com.employee.dto.checkin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutRequest {
    private Double latitude;
    private Double longitude;
    private String notes;  // Optional notes about the day's work
}