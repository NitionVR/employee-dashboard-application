package com.employee.repository;

import com.employee.model.checkin.OfficeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficeLocationRepository extends JpaRepository<OfficeLocation, Long> {
    List<OfficeLocation> findByIsActiveTrue();
}
