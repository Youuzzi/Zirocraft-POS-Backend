package com.zirocraft.billingsoftware.repository;

import com.zirocraft.billingsoftware.entity.ShiftEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShiftRepository extends JpaRepository<ShiftEntity, Long> {

    // Cari shift yang masih OPEN untuk user tertentu
    Optional<ShiftEntity> findByUserIdAndStatus(String userId, String status);

    // LOGIC BARU: Ambil semua riwayat shift yang sudah tutup untuk diaudit Admin
    // Urutkan berdasarkan waktu tutup (EndTime) yang paling baru di atas (Desc)
    List<ShiftEntity> findByStatusOrderByEndTimeDesc(String status);
}