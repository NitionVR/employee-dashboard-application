package com.employee.controller.timesheet;

import com.employee.dto.timesheet.TimeEntryAggregateDTO;
import com.employee.dto.timesheet.TimeEntryDTO;
import com.employee.dto.timesheet.TimeEntryRequest;
import com.employee.service.TimeEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {
    private final TimeEntryService timeEntryService;

    @PostMapping
    public ResponseEntity<TimeEntryDTO> logTime(
            @RequestBody TimeEntryRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(timeEntryService.logTime(principal.getName(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeEntryDTO> updateTimeEntry(
            @PathVariable Long id,
            @RequestBody TimeEntryRequest request,
            Principal principal
    ) {
        return ResponseEntity.ok(timeEntryService.updateTimeEntry(principal.getName(), id, request));
    }

    @GetMapping
    public ResponseEntity<List<TimeEntryDTO>> getTimeEntries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal
    ) {
        return ResponseEntity.ok(timeEntryService.getTimeEntries(principal.getName(), startDate, endDate));
    }

    @GetMapping("/graph")
    public ResponseEntity<List<TimeEntryAggregateDTO>> getTimeEntriesForGraph(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Principal principal
    ) {
        return ResponseEntity.ok(timeEntryService.getTimeEntriesForGraph(
                principal.getName(), startDate, endDate));
    }

    @GetMapping("/monthly-total/{year}/{month}")
    public ResponseEntity<Map<String, Double>> getMonthlyTotal(
            @PathVariable int year,
            @PathVariable int month,
            Principal principal
    ) {
        return ResponseEntity.ok(timeEntryService.getMonthlyTotal(
                principal.getName(), year, month));
    }
}