package com.employee.service;

import com.employee.dto.*;
import com.employee.dto.meeting.MeetingDTO;
import com.employee.dto.timesheet.TimeEntryAggregateDTO;
import com.employee.dto.timesheet.TimeEntryDTO;
import com.employee.dto.timesheet.TimeEntryRequest;
import com.employee.dto.user.UserDTO;
import com.employee.dto.user.UserStatisticsDTO;
import com.employee.model.meeting.Meeting;
import com.employee.model.timesheet.TimeEntry;
import com.employee.model.user.User;
import com.employee.repository.MeetingRepository;
import com.employee.repository.TimeEntryRepository;
import com.employee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeEntryService {
    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;
    private  final MeetingRepository meetingRepository;

    public TimeEntryDTO logTime(String userEmail, TimeEntryRequest request) {
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot log time for future dates");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TimeEntry timeEntry = TimeEntry.builder()
                .user(user)
                .date(request.getDate())
                .hours(request.getHours())
                .description(request.getDescription())
                .build();

        timeEntry = timeEntryRepository.save(timeEntry);
        return mapToDTO(timeEntry);
    }

    public TimeEntryDTO updateTimeEntry(String userEmail, Long timeEntryId, TimeEntryRequest request) {
        TimeEntry timeEntry = timeEntryRepository.findById(timeEntryId)
                .orElseThrow(() -> new RuntimeException("Time entry not found"));

        if (!timeEntry.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to update this time entry");
        }

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot log time for future dates");
        }

        timeEntry.setDate(request.getDate());
        timeEntry.setHours(request.getHours());
        timeEntry.setDescription(request.getDescription());

        timeEntry = timeEntryRepository.save(timeEntry);
        return mapToDTO(timeEntry);
    }

    public List<TimeEntryDTO> getTimeEntries(String userEmail, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return timeEntryRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TimeEntryDTO mapToDTO(TimeEntry timeEntry) {
        return TimeEntryDTO.builder()
                .id(timeEntry.getId())
                .date(timeEntry.getDate())
                .hours(timeEntry.getHours())
                .description(timeEntry.getDescription())
                .createdAt(timeEntry.getCreatedAt())
                .updatedAt(timeEntry.getUpdatedAt())
                .build();
    }

    public List<TimeEntryAggregateDTO> getTimeEntriesForGraph(String userEmail, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return timeEntryRepository.findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate)
                .stream()
                .map(entry -> TimeEntryAggregateDTO.builder()
                        .date(entry.getDate())
                        .hours(entry.getHours())
                        .build())
                .collect(Collectors.toList());
    }

    public Map<String, Double> getMonthlyTotal(String userEmail, int year, int month) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        Double totalHours = timeEntryRepository
                .findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate)
                .stream()
                .mapToDouble(TimeEntry::getHours)
                .sum();

        return Map.of("totalHours", totalHours);
    }

    public AdminStatisticsDTO getAdminStatistics(LocalDate startDate, LocalDate endDate) {
        List<User> allUsers = userRepository.findAll();
        List<TimeEntry> timeEntries = timeEntryRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
        List<Meeting> meetings = meetingRepository.findByStartTimeBetweenOrderByStartTimeDesc(
                startDate.atStartOfDay(),
                endDate.atTime(23, 59, 59)
        );

        Map<String, Double> hoursPerUser = timeEntries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getUser().getEmail(),
                        Collectors.summingDouble(TimeEntry::getHours)
                ));

        Map<LocalDate, Double> hoursPerDay = timeEntries.stream()
                .collect(Collectors.groupingBy(
                        TimeEntry::getDate,
                        Collectors.summingDouble(TimeEntry::getHours)
                ));

        Double totalHours = timeEntries.stream()
                .mapToDouble(TimeEntry::getHours)
                .sum();

        return AdminStatisticsDTO.builder()
                .totalHoursLogged(totalHours)
                .totalUsers(allUsers.size())
                .totalTimeEntries(timeEntries.size())
                .totalMeetings(meetings.size())
                .hoursPerUser(hoursPerUser)
                .hoursPerDay(hoursPerDay)
                .build();
    }

    public UserStatisticsDTO getUserStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<TimeEntry> timeEntries = timeEntryRepository
                .findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);

        List<Meeting> meetings = meetingRepository
                .findByParticipantsContainingAndStartTimeBetween(
                        user,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59)
                );

        Map<LocalDate, Double> hoursPerDay = timeEntries.stream()
                .collect(Collectors.groupingBy(
                        TimeEntry::getDate,
                        Collectors.summingDouble(TimeEntry::getHours)
                ));

        Double totalHours = timeEntries.stream()
                .mapToDouble(TimeEntry::getHours)
                .sum();

        long daysWithEntries = timeEntries.stream()
                .map(TimeEntry::getDate)
                .distinct()
                .count();

        Double averageHoursPerDay = daysWithEntries > 0
                ? totalHours / daysWithEntries
                : 0.0;

        List<Meeting> upcomingMeetings = meetings.stream()
                .filter(m -> m.getStartTime().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Meeting::getStartTime))
                .toList();

        return UserStatisticsDTO.builder()
                .user(mapUserToDTO(user))
                .totalHoursLogged(totalHours)
                .totalTimeEntries(timeEntries.size())
                .totalMeetings(meetings.size())
                .hoursPerDay(hoursPerDay)
                .upcomingMeetings(upcomingMeetings.stream()
                        .map(this::mapMeetingToDTO)
                        .collect(Collectors.toList()))
                .averageHoursPerDay(averageHoursPerDay)
                .build();
    }

    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .birthday(user.getBirthday())
                .role(user.getRole())
                .build();
    }

    private MeetingDTO mapMeetingToDTO(Meeting meeting) {
        return MeetingDTO.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .createdBy(mapUserToDTO(meeting.getCreatedBy()))
                .participants(meeting.getParticipants().stream()
                        .map(this::mapUserToDTO)
                        .collect(Collectors.toSet()))
                .createdAt(meeting.getCreatedAt())
                .updatedAt(meeting.getUpdatedAt())
                .build();
    }

    public List<TimeEntry> getAllTimeEntries(LocalDate startDate, LocalDate endDate) {
        return timeEntryRepository.findByDateBetweenOrderByDateDesc(startDate, endDate);
    }
}