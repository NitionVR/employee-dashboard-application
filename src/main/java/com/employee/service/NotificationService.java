package com.employee.service;

import com.employee.model.attendance.AttendanceRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    @Async
    public void notifyForcedCheckOut(String userEmail, AttendanceRecord record) {
        log.info("Sending forced check-out notification to: {}", userEmail);
        // In real implementation:
        // 1. Send email
        // 2. Push notification
        // 3. In-app notification
    }
}
