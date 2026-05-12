package com.group5.afes.repository;

import com.group5.afes.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    // Tự động sinh query lấy 1 bản ghi có ID lớn nhất
    SensorData findTopByOrderByIdDesc();
    SensorData findTopByRoom_IdOrderByIdDesc(Integer roomId);
    java.util.List<SensorData> findByRoom_IdOrderByTimestampDesc(Integer roomId);

    SensorData findTopByRoom_IdAndTopicContainingOrderByTimestampDesc(Integer roomId, String topic);

    SensorData findTopByRoom_IdAndTopicContainingAndSensorNameOrderByTimestampDesc(
            Integer roomId,
            String topic,
            String sensorName
    );

    SensorData findTopByRoom_IdAndSensorNameContainingIgnoreCaseOrderByTimestampDesc(
            Integer roomId, String sensorName);

    long countByRoom_Id(Integer roomId);
}