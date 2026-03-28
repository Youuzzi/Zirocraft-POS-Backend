package com.zirocraft.billingsoftware.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tbl_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Database yang ngatur ID
    private Long id;

    private String storeName;
    private BigDecimal defaultFloatAmount;
    private String currencySymbol;
}