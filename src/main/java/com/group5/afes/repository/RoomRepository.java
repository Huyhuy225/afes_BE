package com.group5.afes.repository;

import com.group5.afes.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {
    Optional<Room> findByCode(String code);
    Optional<Room> findByCodeIgnoreCase(String code);
}