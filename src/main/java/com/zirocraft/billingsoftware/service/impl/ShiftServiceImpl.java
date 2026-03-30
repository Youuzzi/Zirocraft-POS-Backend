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

    // Menarik data shift yang masih status OPEN untuk user tertentu
    public Optional<ShiftEntity> getCurrentShift(String userId) {
        return shiftRepository.findByUserIdAndStatus(userId, "OPEN");
    }

    // Membuka Shift Baru
    @Transactional
    public ShiftEntity openShift(String userId, BigDecimal openingBalance) {
        // Cek dulu, kalau ada yang masih OPEN, jangan buat baru, return yang lama saja
        Optional<ShiftEntity> existing = getCurrentShift(userId);
        if (existing.isPresent()) return existing.get();

        // Buat Sesi Shift Baru dengan angka awal yang bersih
        ShiftEntity newShift = ShiftEntity.builder()
                .userId(userId)
                .openingBalance(openingBalance)
                .totalSales(BigDecimal.ZERO)    // Paksa Nol di awal
                .totalExpenses(BigDecimal.ZERO) // Paksa Nol di awal
                .status("OPEN")
                .build();

        return shiftRepository.save(newShift);
    }

    // Mencatat Pengeluaran (Petty Cash)
    @Transactional
    public ExpenseEntity addExpense(Long shiftId, String description, BigDecimal amount, String userId) {
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Error: Sesi Shift tidak aktif!"));

        // 1. Catat ke tabel detail pengeluaran
        ExpenseEntity expense = ExpenseEntity.builder()
                .shiftId(shiftId)
                .description(description)
                .amount(amount)
                .userId(userId)
                .build();

        expenseRepository.save(expense);

        // 2. Akumulasi total pengeluaran ke dalam tabel shift
        BigDecimal currentExpenses = shift.getTotalExpenses() != null ? shift.getTotalExpenses() : BigDecimal.ZERO;
        shift.setTotalExpenses(currentExpenses.add(amount));

        shiftRepository.save(shift);
        return expense;
    }

    // Menutup Shift (Closing Shift)
    @Transactional
    public ShiftEntity closeShift(Long shiftId, BigDecimal actualPhysicalCash) {
        ShiftEntity shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new RuntimeException("Error: Shift tidak ditemukan!"));

        // LOGIKA REKONSILIASI KAS (Anti-Korupsi):
        // Uang seharusnya ada = Modal Awal + Penjualan - Pengeluaran
        BigDecimal expected = shift.getOpeningBalance()
                .add(shift.getTotalSales())
                .subtract(shift.getTotalExpenses());

        shift.setExpectedBalance(expected);
        shift.setActualBalance(actualPhysicalCash);

        // Variance = Uang yang dilaporkan kasir - Uang hitungan sistem
        // Jika minus = Kasir Nombok. Jika plus = Ada uang lebih (tips/salah kembalian)
        shift.setVariance(actualPhysicalCash.subtract(expected));

        shift.setEndTime(new Timestamp(System.currentTimeMillis()));
        shift.setStatus("CLOSED");

        return shiftRepository.save(shift);
    }
}