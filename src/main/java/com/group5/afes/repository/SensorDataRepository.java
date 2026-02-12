package com.group5.afes.repository;

import com.group5.afes.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    // JpaRepository đã cung cấp sẵn các hàm save(), findAll(), findById()...
}