package com.zirocraft.billingsoftware.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "tbl_expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long shiftId;
    private String description;
    private BigDecimal amount;
    private String userId; // Email kasir yang mencatat

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp createdAt;
}