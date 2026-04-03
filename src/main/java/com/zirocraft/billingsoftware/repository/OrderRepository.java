package com.zirocraft.billingsoftware.repository;

import com.zirocraft.billingsoftware.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.sql.Timestamp;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    @Query("SELECT MAX(o.queueNumber) FROM OrderEntity o WHERE o.createdAt BETWEEN :start AND :end")
    Integer findMaxQueueNumberToday(@Param("start") Timestamp start, @Param("end") Timestamp end);

    List<OrderEntity> findByCreatedAtBetweenOrderByIdDesc(Timestamp start, Timestamp end);

    // --- TAMBAHKAN INI UNTUK PENCARIAN ---
    List<OrderEntity> findByOrderNumberContainingIgnoreCaseOrCustomerNameContainingIgnoreCaseOrderByIdDesc(String orderNum, String custName);

    List<OrderEntity> findByShiftIdOrderByIdDesc(Long shiftId);

    boolean existsByIdempotencyKey(String idempotencyKey);
}