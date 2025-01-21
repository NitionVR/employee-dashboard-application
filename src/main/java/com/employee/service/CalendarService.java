package com.employee.service;

import com.employee.dto.CalendarEventDTO;
import com.employee.dto.meeting.MeetingDTO;
import com.employee.dto.meeting.MeetingRequest;
import com.employee.dto.user.UserDTO;
import com.employee.model.meeting.Meeting;
import com.employee.model.timesheet.TimeEntry;
import com.employee.model.user.User;
import com.employee.repository.MeetingRepository;
import com.employee.repository.TimeEntryRepository;
import com.employee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {
    private final MeetingRepository meetingRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final UserRepository userRepository;

    public List<CalendarEventDTO> getCalendarEvents(
            String userEmail,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CalendarEventDTO> events = new ArrayList<>();

        // Add meetings
        List<Meeting> meetings = meetingRepository
                .findByParticipantsContainingAndStartTimeBetween(user, startDateTime, endDateTime);

        events.addAll(meetings.stream()
                .map(meeting -> CalendarEventDTO.builder()
                        .id(meeting.getId())
                        .title(meeting.getTitle())
                        .description(meeting.getDescription())
                        .startTime(meeting.getStartTime())
                        .endTime(meeting.getEndTime())
                        .type("MEETING")
                        .build())
                .collect(Collectors.toList()));

        // Add time entries
        List<TimeEntry> timeEntries = timeEntryRepository
                .findByUserAndDateBetweenOrderByDateDesc(
                        user,
                        startDateTime.toLocalDate(),
                        endDateTime.toLocalDate()
                );

        events.addAll(timeEntries.stream()
                .map(entry -> CalendarEventDTO.builder()
                        .id(entry.getId())
                        .title("Work Hours")
                        .description(entry.getDescription())
                        .startTime(entry.getDate().atStartOfDay())
                        .endTime(entry.getDate().atStartOfDay().plusHours(entry.getHours().longValue()))
                        .type("TIME_ENTRY")
                        .hours(entry.getHours())
                        .build())
                .collect(Collectors.toList()));

        return events;
    }

    public MeetingDTO createMeeting(String userEmail, MeetingRequest request) {
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Set<User> participants = request.getParticipantIds().stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Participant not found: " + id)))
                .collect(Collectors.toSet());

        participants.add(creator); // Add creator to participants

        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .createdBy(creator)
                .participants(participants)
                .build();

        meeting = meetingRepository.save(meeting);
        return mapToDTO(meeting);
    }

    private MeetingDTO mapToDTO(Meeting meeting) {
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

    private UserDTO mapUserToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}