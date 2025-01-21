package com.employee.dto.meeting;

import com.employee.dto.user.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private UserDTO createdBy;
    private Set<UserDTO> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
