package com.zirocraft.billingsoftware.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "tbl_shifts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId; // ID Kasir yang bertugas

    @CreationTimestamp
    @Column(updatable = false)
    private Timestamp startTime;

    private Timestamp endTime;

    // --- LOGIKA UANG ---
    private BigDecimal openingBalance;  // Modal saat laci dibuka
    private BigDecimal totalSales;      // Hitungan otomatis sistem dari transaksi
    private BigDecimal totalExpenses;   // Hitungan otomatis dari petty cash

    private BigDecimal expectedBalance; // Hitungan sistem: (Opening + Sales - Expense)
    private BigDecimal actualBalance;   // Input manual Kasir pas mau pulang (Blind closing)

    private BigDecimal variance;        // Selisih (Actual - Expected)

    private String status; // "OPEN" atau "CLOSED"
}