package com.employee.controller.user;

import com.employee.dto.auth.UpdateProfileRequest;
import com.employee.dto.user.UserDTO;
import com.employee.model.user.UserRole;
import com.employee.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(Principal principal) {
        return ResponseEntity.ok(userService.getUserProfile(principal.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @RequestBody UpdateProfileRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), request));
    }

    // Temporary endpoint for testing
    @PostMapping("/make-admin")
    public ResponseEntity<UserDTO> makeAdmin(Principal principal) {
        return ResponseEntity.ok(userService.updateUserRole(1L, UserRole.ADMIN));
    }
}