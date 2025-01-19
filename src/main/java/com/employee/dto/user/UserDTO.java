package com.employee.dto.user;

import com.employee.model.user.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

// dto/UserDTO.java
@Data
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate birthday;
    private UserRole role;
}