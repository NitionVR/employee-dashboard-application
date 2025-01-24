package com.employee.repository;


import com.employee.model.attendance.AttendanceRecord;
import com.employee.model.attendance.AttendanceStatus;
import com.employee.model.checkin.OfficeLocation;
import com.employee.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByUserAndCheckInTimeBetweenOrderByCheckInTimeDesc(
            User user,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<AttendanceRecord> findByCheckInTimeBetweenOrderByCheckInTimeDesc(
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    List<AttendanceRecord> findByOfficeAndCheckInTimeBetweenOrderByCheckInTimeDesc(
            OfficeLocation office,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // Add this method for checking if user already checked in
    Optional<AttendanceRecord> findByUserAndCheckInTimeBetween(
            User user,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // Additional useful method for finding latest attendance record
    Optional<AttendanceRecord> findFirstByUserOrderByCheckInTimeDesc(User user);

    // Find unchecked-out records
    Optional<AttendanceRecord> findByUserAndStatusAndCheckInTimeBetween(
            User user,
            AttendanceStatus status,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    Optional<AttendanceRecord> findFirstByUserAndStatusOrderByCheckInTimeDesc(
            User user,
            AttendanceStatus status
    );
}