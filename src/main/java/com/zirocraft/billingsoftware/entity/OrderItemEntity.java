package com.zirocraft.billingsoftware.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_order_items")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subTotal;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore // Penting: Biar gak infinite loop pas jadi JSON
    private OrderEntity order;
}