package com.employee.service;


import com.employee.dto.meeting.MeetingDTO;
import com.employee.dto.user.UserDTO;
import com.employee.model.meeting.Meeting;
import com.employee.model.user.User;
import com.employee.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;

    public List<MeetingDTO> getAllMeetings(LocalDate startDate, LocalDate endDate) {
        return meetingRepository.findByStartTimeBetweenOrderByStartTimeDesc(
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59)
                ).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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