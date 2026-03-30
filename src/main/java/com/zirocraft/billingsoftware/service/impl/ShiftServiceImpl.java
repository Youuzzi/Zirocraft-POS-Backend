package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.ExpenseEntity;
import com.zirocraft.billingsoftware.entity.ShiftEntity;
import com.zirocraft.billingsoftware.repository.ExpenseRepository;
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
    private final ExpenseRepository expenseRepository;

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
    public ExpenseEntity addExpense(Long shiftId, String description, BigDecimal amount, String userId) {
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift tidak ditemukan!"));

        // 1. Simpan detail pengeluaran ke tabel expense
        ExpenseEntity expense = ExpenseEntity.builder()
                .shiftId(shiftId)
                .description(description)
                .amount(amount)
                .userId(userId)
                .build();

        expenseRepository.save(expense);

        // 2. Update total_expenses di tabel shift secara akumulatif
        BigDecimal currentExpenses = shift.getTotalExpenses() != null ? shift.getTotalExpenses() : BigDecimal.ZERO;
        shift.setTotalExpenses(currentExpenses.add(amount));
        shiftRepository.save(shift);

        return expense;
    }

    @Transactional
    public ShiftEntity closeShift(Long shiftId, BigDecimal actualPhysicalCash) {
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Shift tidak ditemukan!"));

        BigDecimal expected = shift.getOpeningBalance()
                .add(shift.getTotalSales())
                .subtract(shift.getTotalExpenses());

        shift.setExpectedBalance(expected);
        shift.setActualBalance(actualPhysicalCash);
        shift.setVariance(actualPhysicalCash.subtract(expected));
        shift.setEndTime(new Timestamp(System.currentTimeMillis()));
        shift.setStatus("CLOSED");

        return shiftRepository.save(shift);
    }
}