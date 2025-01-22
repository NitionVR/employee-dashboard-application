package com.employee.model.attendance;

import com.employee.model.checkin.OfficeLocation;
import com.employee.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "office_id")
    private OfficeLocation office;

    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    private Double checkInLatitude;
    private Double checkInLongitude;

    private Double checkOutLatitude;
    private Double checkOutLongitude;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private String notes;
}

