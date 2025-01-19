package com.employee.service.user;

import com.employee.dto.meeting.MeetingDTO;
import com.employee.dto.auth.UpdateProfileRequest;
import com.employee.dto.user.UserDTO;
import com.employee.dto.user.UserStatisticsDTO;
import com.employee.model.meeting.Meeting;
import com.employee.model.timesheet.TimeEntry;
import com.employee.model.user.User;
import com.employee.model.user.UserRole;
import com.employee.repository.MeetingRepository;
import com.employee.repository.TimeEntryRepository;
import com.employee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final MeetingRepository meetingRepository;

    public UserDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToDTO(user);
    }

    public UserDTO updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setBirthday(request.getBirthday());

        user = userRepository.save(user);
        return mapToDTO(user);
    }



    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO updateUserRole(Long userId, UserRole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(newRole);
        user = userRepository.save(user);

        SecurityContextHolder.clearContext();

        return mapToDTO(user);
    }

    public List<UserStatisticsDTO> getAllUsersStatistics(LocalDate startDate, LocalDate endDate) {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> getUserStatisticsForUser(user, startDate, endDate))
                .collect(Collectors.toList());
    }

    private UserStatisticsDTO getUserStatisticsForUser(User user, LocalDate startDate, LocalDate endDate) {
        // Get time entries
        List<TimeEntry> timeEntries = timeEntryRepository
                .findByUserAndDateBetweenOrderByDateDesc(user, startDate, endDate);

        // Get meetings
        List<Meeting> meetings = meetingRepository
                .findByParticipantsContainingAndStartTimeBetween(
                        user,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59)
                );

        // Calculate statistics
        Double totalHours = timeEntries.stream()
                .mapToDouble(TimeEntry::getHours)
                .sum();

        Map<LocalDate, Double> hoursPerDay = timeEntries.stream()
                .collect(Collectors.groupingBy(
                        TimeEntry::getDate,
                        Collectors.summingDouble(TimeEntry::getHours)
                ));

        long daysWorked = timeEntries.stream()
                .map(TimeEntry::getDate)
                .distinct()
                .count();

        Double averageHoursPerDay = daysWorked > 0 ? totalHours / daysWorked : 0.0;

        List<Meeting> upcomingMeetings = meetings.stream()
                .filter(m -> m.getStartTime().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Meeting::getStartTime))
                .collect(Collectors.toList());

        return UserStatisticsDTO.builder()
                .user(mapToDTO(user))
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

    private UserDTO mapToDTO(User user) {
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
                .createdBy(mapToDTO(meeting.getCreatedBy()))
                .participants(meeting.getParticipants().stream()
                        .map(this::mapToDTO)
                        .collect(Collectors.toSet()))
                .createdAt(meeting.getCreatedAt())
                .updatedAt(meeting.getUpdatedAt())
                .build();
    }
}