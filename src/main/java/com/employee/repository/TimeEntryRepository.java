package com.employee.repository;

import com.employee.model.timesheet.TimeEntry;
import com.employee.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// repository/TimeEntryRepository.java
// TimeEntryRepository.java
@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate startDate, LocalDate endDate);
    Optional<TimeEntry> findByUserAndDate(User user, LocalDate date);
    boolean existsByUserAndDate(User user, LocalDate date);

    // Add this new method
    List<TimeEntry> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);
}