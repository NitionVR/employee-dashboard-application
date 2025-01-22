package com.employee.model.checkin;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "office_locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfficeLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String address;

    // Office center point
    private Double latitude;
    private Double longitude;

    // Radius in meters within which check-in is allowed
    private Double allowedRadius;

    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

