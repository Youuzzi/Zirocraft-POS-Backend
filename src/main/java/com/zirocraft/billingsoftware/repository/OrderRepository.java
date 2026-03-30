package com.zirocraft.billingsoftware.repository;

import com.zirocraft.billingsoftware.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.sql.Timestamp;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // Mencari nomor antrean tertinggi hari ini
    @Query("SELECT MAX(o.queueNumber) FROM OrderEntity o WHERE o.createdAt BETWEEN :start AND :end")
    Integer findMaxQueueNumberToday(@Param("start") Timestamp start, @Param("end") Timestamp end);
}