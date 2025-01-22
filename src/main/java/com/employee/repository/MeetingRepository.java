package com.employee.repository;

import com.employee.model.meeting.Meeting;
import com.employee.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

// MeetingRepository.java
@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByParticipantsContainingAndStartTimeBetween(
            User participant,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // Add this new method
    List<Meeting> findByStartTimeBetweenOrderByStartTimeDesc(
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}