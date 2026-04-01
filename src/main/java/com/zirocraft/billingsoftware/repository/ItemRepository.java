package com.zirocraft.billingsoftware.repository;

import com.zirocraft.billingsoftware.entity.ItemEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<ItemEntity, Long> {
    Optional<ItemEntity> findByItemId(String id);
    Integer countByCategoryId(Long id);
    Optional<ItemEntity> findByName(String name);

    // LOGIC BARU: PESSIMISTIC LOCKING. Gembok baris database ini sampai transaksi selesai.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM ItemEntity i WHERE i.name = :name")
    Optional<ItemEntity> findByNameForUpdate(@Param("name") String name);
}