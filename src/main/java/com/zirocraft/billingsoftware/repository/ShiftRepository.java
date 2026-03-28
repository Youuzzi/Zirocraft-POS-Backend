package com.zirocraft.billingsoftware.repository;

import com.zirocraft.billingsoftware.entity.ShiftEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<ShiftEntity, Long> {
    // Cari shift yang statusnya OPEN berdasarkan user
    Optional<ShiftEntity> findByUserIdAndStatus(String userId, String status);
}