package com.employee.service.attendance;

import com.employee.dto.attendance.AttendanceRecordDTO;
import com.employee.dto.attendance.AttendanceStatusDTO;
import com.employee.dto.checkin.CheckInRequest;
import com.employee.dto.checkin.CheckOutRequest;
import com.employee.exception.InvalidLocationException;
import com.employee.model.attendance.AttendanceRecord;
import com.employee.model.attendance.AttendanceStatus;
import com.employee.model.checkin.OfficeLocation;
import com.employee.model.user.User;
import com.employee.repository.AttendanceRecordRepository;
import com.employee.repository.OfficeLocationRepository;
import com.employee.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final OfficeLocationRepository officeLocationRepository;
    private final UserRepository userRepository;

    public AttendanceRecordDTO checkIn(String userEmail, CheckInRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        OfficeLocation office = officeLocationRepository.findById(request.getOfficeId())
                .orElseThrow(() -> new RuntimeException("Office location not found"));

        // Validate if already checked in today
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        Optional<AttendanceRecord> existingRecord = attendanceRecordRepository
                .findByUserAndCheckInTimeBetween(user, startOfDay, endOfDay);

        if (existingRecord.isPresent()) {
            AttendanceRecord record = existingRecord.get();
            if (record.getStatus() == AttendanceStatus.CHECKED_IN) {
                throw new RuntimeException("You are already checked in. Please check out first.");
            } else if (record.getStatus() == AttendanceStatus.CHECKED_OUT) {
                throw new RuntimeException("You have already completed your attendance for today.");
            }
        }

        // Check for any unchecked-out records from previous days
        Optional<AttendanceRecord> uncheckedRecord = attendanceRecordRepository
                .findByUserAndStatusAndCheckInTimeBetween(
                        user,
                        AttendanceStatus.CHECKED_IN,
                        LocalDateTime.now().minusDays(7), // Look back 7 days
                        startOfDay
                );

        if (uncheckedRecord.isPresent()) {
            log.warn("User {} has unchecked-out record from {}",
                    userEmail,
                    uncheckedRecord.get().getCheckInTime());
            // Optionally auto-checkout the previous record
            autoCheckoutPreviousRecord(uncheckedRecord.get());
        }

        // Validate location
        if (!isWithinOfficeRadius(
                request.getLatitude(),
                request.getLongitude(),
                office.getLatitude(),
                office.getLongitude(),
                office.getAllowedRadius())) {

            // Create record with invalid location status
            AttendanceRecord record = AttendanceRecord.builder()
                    .user(user)
                    .office(office)
                    .checkInTime(LocalDateTime.now())
                    .checkInLatitude(request.getLatitude())
                    .checkInLongitude(request.getLongitude())
                    .status(AttendanceStatus.INVALID_LOCATION)
                    .build();

            record = attendanceRecordRepository.save(record);
            throw new InvalidLocationException("Location is outside office premises", record.getId());
        }

        // Create attendance record
        AttendanceRecord record = AttendanceRecord.builder()
                .user(user)
                .office(office)
                .checkInTime(LocalDateTime.now())
                .checkInLatitude(request.getLatitude())
                .checkInLongitude(request.getLongitude())
                .status(AttendanceStatus.CHECKED_IN)
                .build();

        record = attendanceRecordRepository.save(record);
        return mapToDTO(record);
    }

    public AttendanceRecordDTO checkOut(String userEmail, CheckOutRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find today's check-in record
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        AttendanceRecord record = attendanceRecordRepository
                .findByUserAndCheckInTimeBetween(user, startOfDay, endOfDay)
                .orElseThrow(() -> new RuntimeException("No check-in record found for today"));

        // Validate not already checked out
        if (record.getStatus() == AttendanceStatus.CHECKED_OUT) {
            throw new RuntimeException("Already checked out for today");
        }

        // Validate location
        if (!isWithinOfficeRadius(
                request.getLatitude(),
                request.getLongitude(),
                record.getOffice().getLatitude(),
                record.getOffice().getLongitude(),
                record.getOffice().getAllowedRadius())) {
            log.warn("User attempting to check out from invalid location: {}, {}",
                    request.getLatitude(), request.getLongitude());
            // Optionally: You might want to allow check-out from any location
            // or create a separate status for out-of-office check-out
        }

        // Calculate work duration
        long minutesWorked = ChronoUnit.MINUTES.between(
                record.getCheckInTime(),
                LocalDateTime.now()
        );

        // Optionally: Validate minimum work hours
        if (minutesWorked < 60) { // Example: minimum 1 hour
            throw new RuntimeException("Minimum work duration not met");
        }

        // Update record
        record.setCheckOutTime(LocalDateTime.now());
        record.setCheckOutLatitude(request.getLatitude());
        record.setCheckOutLongitude(request.getLongitude());
        record.setStatus(AttendanceStatus.CHECKED_OUT);
        record.setNotes(request.getNotes());

        record = attendanceRecordRepository.save(record);

        // Log successful check-out
        log.info("User {} checked out successfully. Hours worked: {}",
                userEmail, minutesWorked / 60.0);

        return mapToDTO(record);
    }

    // Add method to get current status
    public AttendanceStatusDTO getCurrentStatus(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1);

        return attendanceRecordRepository
                .findByUserAndCheckInTimeBetween(user, startOfDay, endOfDay)
                .map(record -> AttendanceStatusDTO.builder()
                        .date(LocalDate.now())
                        .status(record.getStatus())
                        .checkInTime(record.getCheckInTime())
                        .checkOutTime(record.getCheckOutTime())
                        .hoursWorked(record.getCheckOutTime() != null ?
                                ChronoUnit.MINUTES.between(
                                        record.getCheckInTime(),
                                        record.getCheckOutTime()) / 60.0 : null)
                        .build())
                .orElse(AttendanceStatusDTO.builder()
                        .date(LocalDate.now())
                        .status(null)
                        .build());
    }

    private void autoCheckoutPreviousRecord(AttendanceRecord record) {
        record.setCheckOutTime(record.getCheckInTime().plusHours(8)); // Assume 8-hour workday
        record.setStatus(AttendanceStatus.CHECKED_OUT);
        record.setCheckOutLatitude(record.getCheckInLatitude());
        record.setCheckOutLongitude(record.getCheckInLongitude());
        attendanceRecordRepository.save(record);

        log.info("Auto checked-out previous record for user {} from {}",
                record.getUser().getEmail(),
                record.getCheckInTime());
    }


    private boolean isWithinOfficeRadius(
            double userLat,
            double userLng,
            double officeLat,
            double officeLng,
            double radiusInMeters) {

        // Calculate distance using Haversine formula
        double earthRadius = 6371000; // meters

        double dLat = Math.toRadians(officeLat - userLat);
        double dLng = Math.toRadians(officeLng - userLng);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(officeLat)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        return distance <= radiusInMeters;
    }


    public List<AttendanceRecordDTO> getHistory(String userEmail, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Convert dates to LocalDateTime for database query
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        log.info("Fetching attendance history for user {} between {} and {}",
                user.getEmail(), startDateTime, endDateTime);

        List<AttendanceRecord> records = attendanceRecordRepository
                .findByUserAndCheckInTimeBetweenOrderByCheckInTimeDesc(
                        user,
                        startDateTime,
                        endDateTime
                );

        return records.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // For admin use
    public List<AttendanceRecordDTO> getAllAttendanceRecords(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return attendanceRecordRepository.findByCheckInTimeBetweenOrderByCheckInTimeDesc(
                        startDateTime,
                        endDateTime
                )
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // For admin use - get attendance by office
    public List<AttendanceRecordDTO> getAttendanceByOffice(
            Long officeId,
            LocalDate startDate,
            LocalDate endDate) {

        OfficeLocation office = officeLocationRepository.findById(officeId)
                .orElseThrow(() -> new RuntimeException("Office not found"));

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return attendanceRecordRepository.findByOfficeAndCheckInTimeBetweenOrderByCheckInTimeDesc(
                        office,
                        startDateTime,
                        endDateTime
                )
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // Enhanced DTO mapping with more details
    private AttendanceRecordDTO mapToDTO(AttendanceRecord record) {
        return AttendanceRecordDTO.builder()
                .id(record.getId())
                .userName(record.getUser().getFirstName() + " " + record.getUser().getLastName())
                .userEmail(record.getUser().getEmail())
                .officeName(record.getOffice().getName())
                .checkInTime(record.getCheckInTime())
                .checkOutTime(record.getCheckOutTime())
                .status(record.getStatus())
                .checkInLocation(String.format("%.6f, %.6f",
                        record.getCheckInLatitude(),
                        record.getCheckInLongitude()))
                .checkOutLocation(record.getCheckOutLatitude() != null ?
                        String.format("%.6f, %.6f",
                                record.getCheckOutLatitude(),
                                record.getCheckOutLongitude()) : null)
                .totalHours(calculateTotalHours(record))
                .build();
    }

    private Double calculateTotalHours(AttendanceRecord record) {
        if (record.getCheckInTime() == null || record.getCheckOutTime() == null) {
            return null;
        }

        long minutes = ChronoUnit.MINUTES.between(
                record.getCheckInTime(),
                record.getCheckOutTime()
        );

        return minutes / 60.0;
    }

    public AttendanceRecordDTO forceCheckOut(Long userId, CheckOutRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find latest unchecked-out record
        AttendanceRecord record = attendanceRecordRepository
                .findFirstByUserAndStatusOrderByCheckInTimeDesc(user, AttendanceStatus.CHECKED_IN)
                .orElseThrow(() -> new RuntimeException("No active check-in record found for user"));

        log.info("Admin forcing check-out for user: {} (Record ID: {})",
                user.getEmail(), record.getId());

        // Update record
        record.setCheckOutTime(LocalDateTime.now());
        record.setCheckOutLatitude(request.getLatitude());
        record.setCheckOutLongitude(request.getLongitude());
        record.setStatus(AttendanceStatus.CHECKED_OUT);
        record.setNotes(request.getNotes() != null ?
                request.getNotes() + " [Force checked-out by admin]" :
                "[Force checked-out by admin]");

        // Calculate duration
        long minutesWorked = ChronoUnit.MINUTES.between(
                record.getCheckInTime(),
                record.getCheckOutTime()
        );

        log.info("Force check-out completed. Hours worked: {}", minutesWorked / 60.0);

        record = attendanceRecordRepository.save(record);

        // Optionally: Send notification to user
        // notificationService.notifyForcedCheckOut(user.getEmail(), record);

        return mapToDTO(record);
    }
}