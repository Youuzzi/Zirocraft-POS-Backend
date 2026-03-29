package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.ShiftEntity;
import com.zirocraft.billingsoftware.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl {

    private final ShiftRepository shiftRepository;

    public Optional<ShiftEntity> getCurrentShift(String userId) {
        return shiftRepository.findByUserIdAndStatus(userId, "OPEN");
    }

    public ShiftEntity openShift(String userId, BigDecimal openingBalance) {
        Optional<ShiftEntity> existing = getCurrentShift(userId);
        if (existing.isPresent()) return existing.get();

        ShiftEntity newShift = ShiftEntity.builder()
                .userId(userId)
                .openingBalance(openingBalance)
                .totalSales(BigDecimal.ZERO)
                .totalExpenses(BigDecimal.ZERO)
                .status("OPEN")
                .build();

        return shiftRepository.save(newShift);
    }

    @Transactional
    public ShiftEntity closeShift(Long shiftId, BigDecimal actualPhysicalCash) {
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift tidak ditemukan!"));

        // LOGIKA ANTI-KORUPSI:
        // Expected = Modal Awal + Penjualan - Pengeluaran
        BigDecimal expected = shift.getOpeningBalance()
                .add(shift.getTotalSales())
                .subtract(shift.getTotalExpenses());

        shift.setExpectedBalance(expected);
        shift.setActualBalance(actualPhysicalCash);

        // Variance = Apa yang dihitung tangan - Apa yang kata sistem
        shift.setVariance(actualPhysicalCash.subtract(expected));

        shift.setEndTime(new Timestamp(System.currentTimeMillis()));
        shift.setStatus("CLOSED");

        return shiftRepository.save(shift);
    }
}