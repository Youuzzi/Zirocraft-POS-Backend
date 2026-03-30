package com.zirocraft.billingsoftware.repository;

import com.zirocraft.billingsoftware.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.sql.Timestamp;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    // Fungsi untuk menghitung orderan di rentang waktu tertentu (Daily Queue)
    long countByCreatedAtBetween(Timestamp start, Timestamp end);
}