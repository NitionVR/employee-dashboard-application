package com.employee.service;

import com.employee.dto.user.UserStatisticsDTO;
import com.employee.model.timesheet.TimeEntry;
import com.employee.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportService {
    private final TimeEntryService timeEntryService;
    private final UserService userService;

    public byte[] generateReport(LocalDate startDate, LocalDate endDate) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            // Time Entries Sheet
            Sheet timeEntriesSheet = workbook.createSheet("Time Entries");
            createTimeEntriesSheet(timeEntriesSheet, startDate, endDate);

            // User Statistics Sheet
            Sheet userStatsSheet = workbook.createSheet("User Statistics");
            createUserStatisticsSheet(userStatsSheet, startDate, endDate);

            // Write to byte array
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

    private void createTimeEntriesSheet(Sheet sheet, LocalDate startDate, LocalDate endDate) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Date");
        headerRow.createCell(1).setCellValue("User");
        headerRow.createCell(2).setCellValue("Hours");
        headerRow.createCell(3).setCellValue("Description");

        // Add data rows
        List<TimeEntry> entries = timeEntryService.getAllTimeEntries(startDate, endDate);
        int rowNum = 1;
        for (TimeEntry entry : entries) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(entry.getDate().toString());
            row.createCell(1).setCellValue(entry.getUser().getEmail());
            row.createCell(2).setCellValue(entry.getHours());
            row.createCell(3).setCellValue(entry.getDescription());
        }
    }

    private void createUserStatisticsSheet(Sheet sheet, LocalDate startDate, LocalDate endDate) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("User");
        headerRow.createCell(1).setCellValue("Total Hours");
        headerRow.createCell(2).setCellValue("Average Hours/Day");
        headerRow.createCell(3).setCellValue("Total Meetings");

        List<UserStatisticsDTO> statistics = userService.getAllUsersStatistics(startDate, endDate);
        int rowNum = 1;
        for (UserStatisticsDTO stat : statistics) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(stat.getUser().getEmail());
            row.createCell(1).setCellValue(stat.getTotalHoursLogged());
            row.createCell(2).setCellValue(stat.getAverageHoursPerDay());
            row.createCell(3).setCellValue(stat.getTotalMeetings());
        }
    }
}