package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.ShiftEntity;
import com.zirocraft.billingsoftware.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl {

    private final ShiftRepository shiftRepository;

    // 1. Cek apakah kasir ini punya shift yang masih terbuka?
    public Optional<ShiftEntity> getCurrentShift(String userId) {
        return shiftRepository.findByUserIdAndStatus(userId, "OPEN");
    }

    // 2. Buka Shift Baru
    public ShiftEntity openShift(String userId, BigDecimal openingBalance) {
        // Cek dulu, kalau ada yang masih OPEN, jangan boleh buka baru
        Optional<ShiftEntity> existing = getCurrentShift(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        ShiftEntity newShift = ShiftEntity.builder()
                .userId(userId)
                .openingBalance(openingBalance)
                .totalSales(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .status("OPEN")
                .build();

        return shiftRepository.save(newShift);
    }
}