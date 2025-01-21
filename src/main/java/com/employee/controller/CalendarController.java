package com.employee.controller;

import com.employee.dto.CalendarEventDTO;
import com.employee.dto.meeting.MeetingDTO;
import com.employee.dto.meeting.MeetingRequest;
import com.employee.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @GetMapping("/events")
    public ResponseEntity<List<CalendarEventDTO>> getCalendarEvents(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            Principal principal
    ) {
        return ResponseEntity.ok(calendarService.getCalendarEvents(
                principal.getName(), startDateTime, endDateTime));
    }

    @PostMapping("/meetings")
    public ResponseEntity<MeetingDTO> createMeeting(
            @RequestBody MeetingRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(calendarService.createMeeting(principal.getName(), request));
    }
}