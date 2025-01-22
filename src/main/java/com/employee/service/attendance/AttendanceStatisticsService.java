package com.employee.service.attendance;

import com.employee.dto.DailyStatsDTO;
import com.employee.dto.DepartmentAttendanceStatsDTO;
import com.employee.dto.user.UserAttendanceStatsDTO;
import com.employee.model.attendance.AttendanceRecord;
import com.employee.model.attendance.AttendanceStatus;
import com.employee.model.timesheet.TimeEntry;
import com.employee.model.user.User;
import com.employee.repository.AttendanceRecordRepository;
import com.employee.repository.TimeEntryRepository;
import com.employee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceStatisticsService {
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;

    public UserAttendanceStatsDTO getUserStats(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<AttendanceRecord> attendanceRecords = attendanceRecordRepository
                .findByUserAndCheckInTimeBetweenOrderByCheckInTimeDesc(
                        user,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59)
                );

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByUserAndDateBetweenOrderByDateDesc(
                        user,
                        startDate,
                        endDate
                );

        return UserAttendanceStatsDTO.builder()
                .userId(userId)
                .userName(user.getFirstName() + " " + user.getLastName())
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(ChronoUnit.DAYS.between(startDate, endDate) + 1)
                .presentDays(countPresentDays(attendanceRecords))
                .totalAttendanceHours(calculateTotalAttendanceHours(attendanceRecords))
                .totalTrackedHours(calculateTotalTrackedHours(timeEntries))
                .averageCheckInTime(calculateAverageCheckInTime(attendanceRecords))
                .averageCheckOutTime(calculateAverageCheckOutTime(attendanceRecords))
                .averageWorkHours(calculateAverageWorkHours(attendanceRecords))
                .lateCheckIns(countLateCheckIns(attendanceRecords))
                .earlyCheckOuts(countEarlyCheckOuts(attendanceRecords))
                .dailyStats(generateDailyStats(attendanceRecords, timeEntries))
                .build();
    }



    public DepartmentAttendanceStatsDTO getDepartmentStats(LocalDate startDate, LocalDate endDate) {
        List<User> users = userRepository.findAll();
        List<UserAttendanceStatsDTO> userStats = users.stream()
                .map(user -> getUserStats(user.getId(), startDate, endDate))
                .collect(Collectors.toList());

        return DepartmentAttendanceStatsDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalEmployees(users.size())
                .averageAttendance(calculateAverageAttendance(userStats))
                .averageWorkHours(calculateAverageWorkHoursStats(userStats))
                .userStats(userStats)
                .build();
    }



    private List<DailyStatsDTO> generateDailyStats(
            List<AttendanceRecord> attendanceRecords,
            List<TimeEntry> timeEntries) {

        Map<LocalDate, List<TimeEntry>> timeEntriesByDate = timeEntries.stream()
                .collect(Collectors.groupingBy(TimeEntry::getDate));

        return attendanceRecords.stream()
                .map(record -> {
                    LocalDate date = record.getCheckInTime().toLocalDate();
                    List<TimeEntry> dailyTimeEntries = timeEntriesByDate.
                            getOrDefault(date, Collections.emptyList());

                    return DailyStatsDTO.builder()
                            .date(date)
                            .checkInTime(record.getCheckInTime())
                            .checkOutTime(record.getCheckOutTime())
                            .attendanceHours(calculateAttendanceHours(record))
                            .trackedHours(calculateTrackedHours(dailyTimeEntries))
                            .status(record.getStatus())
                            .isLateCheckIn(isLateCheckIn(record))
                            .isEarlyCheckOut(isEarlyCheckOut(record))
                            .build();
                })
                .collect(Collectors.toList());
    }


    // Helper methods
    private long countPresentDays(List<AttendanceRecord> records) {
        return records.stream()
                .filter(r -> r.getStatus() == AttendanceStatus.CHECKED_OUT)
                .map(r -> r.getCheckInTime().toLocalDate())
                .distinct()
                .count();
    }

    private double calculateTotalAttendanceHours(List<AttendanceRecord> records) {
        return records.stream()
                .filter(r -> r.getCheckOutTime() != null)
                .mapToDouble(this::calculateAttendanceHours)
                .sum();
    }

    private double calculateTotalTrackedHours(List<TimeEntry> entries) {
        return entries.stream()
                .mapToDouble(TimeEntry::getHours)
                .sum();
    }

    private LocalTime calculateAverageCheckInTime(List<AttendanceRecord> records) {
        OptionalDouble avgMinutes = records.stream()
                .mapToDouble(r -> r.getCheckInTime().toLocalTime().toSecondOfDay() / 60.0)
                .average();

        return avgMinutes.isPresent()
                ? LocalTime.ofSecondOfDay((long) (avgMinutes.getAsDouble() * 60))
                : null;
    }

    private boolean isLateCheckIn(AttendanceRecord record) {
        LocalTime checkInTime = record.getCheckInTime().toLocalTime();
        return checkInTime.isAfter(LocalTime.of(9, 0)); // Example: 9 AM start time
    }

    private boolean isEarlyCheckOut(AttendanceRecord record) {
        if (record.getCheckOutTime() == null) return false;
        LocalTime checkOutTime = record.getCheckOutTime().toLocalTime();
        return checkOutTime.isBefore(LocalTime.of(17, 0)); // Example: 5 PM end time
    }

    private int countLateCheckIns(List<AttendanceRecord> attendanceRecords) {
        return (int) attendanceRecords.stream()
                .filter(this::isLateCheckIn)
                .count();
    }

    private int countEarlyCheckOuts(List<AttendanceRecord> attendanceRecords) {
        return (int) attendanceRecords.stream()
                .filter(this::isEarlyCheckOut)
                .count();
    }

    private double calculateAverageWorkHoursStats(List<UserAttendanceStatsDTO> userStats) {
        if (userStats.isEmpty()) {
            return 0.0;
        }
        return userStats.stream()
                .mapToDouble(UserAttendanceStatsDTO::getTotalAttendanceHours)
                .average()
                .orElse(0.0);
    }

    private LocalTime calculateAverageCheckOutTime(List<AttendanceRecord> attendanceRecords) {
        OptionalDouble avgMinutes = attendanceRecords.stream()
                .filter(r -> r.getCheckOutTime() != null)
                .mapToDouble(r -> r.getCheckOutTime().toLocalTime().toSecondOfDay() / 60.0)
                .average();

        return avgMinutes.isPresent()
                ? LocalTime.ofSecondOfDay((long) (avgMinutes.getAsDouble() * 60))
                : null;
    }

    private double calculateAverageAttendance(List<UserAttendanceStatsDTO> userStats) {
        if (userStats.isEmpty()) {
            return 0.0;
        }

        double totalAttendancePercentage = userStats.stream()
                .mapToDouble(stat -> {
                    long totalDays = stat.getTotalDays();
                    return totalDays > 0
                            ? (double) stat.getPresentDays() / totalDays * 100
                            : 0.0;
                })
                .sum();

        return totalAttendancePercentage / userStats.size();
    }

    private double calculateTrackedHours(List<TimeEntry> dailyTimeEntries) {
        return dailyTimeEntries.stream()
                .mapToDouble(TimeEntry::getHours)
                .sum();
    }

    private double calculateAttendanceHours(AttendanceRecord record) {
        if (record.getCheckInTime() == null || record.getCheckOutTime() == null) {
            return 0.0;
        }

        long minutes = ChronoUnit.MINUTES.between(
                record.getCheckInTime(),
                record.getCheckOutTime()
        );

        // Convert minutes to hours
        return minutes / 60.0;
    }

    // Also, let's add this method to calculate average work hours for a list of attendance records
    private double calculateAverageWorkHours(List<AttendanceRecord> attendanceRecords) {
        if (attendanceRecords.isEmpty()) {
            return 0.0;
        }

        double totalHours = attendanceRecords.stream()
                .filter(r -> r.getCheckOutTime() != null)
                .mapToDouble(this::calculateAttendanceHours)
                .sum();

        long daysWithRecords = attendanceRecords.stream()
                .map(r -> r.getCheckInTime().toLocalDate())
                .distinct()
                .count();

        return daysWithRecords > 0 ? totalHours / daysWithRecords : 0.0;
    }

}