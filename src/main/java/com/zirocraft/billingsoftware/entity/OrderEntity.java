package com.zirocraft.billingsoftware.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Table(name = "tbl_orders")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    private String tableNumber;

    // --- FINANCIAL BREAKDOWN ---
    private BigDecimal subTotal;
    private BigDecimal serviceCharge;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private String paymentType;
    private String userId;
    private Long shiftId;

    @Column(nullable = false)
    private String status;

    private Integer queueNumber;

    // --- LOGIC MASTER AUDIT: Idempotency Key (Anti Double-Submit) ---
    @Column(unique = true)
    private String idempotencyKey;

    private String voidReason;
    private String voidBy;

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItemEntity> items;
}