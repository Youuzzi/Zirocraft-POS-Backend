package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.ExpenseEntity;
import com.zirocraft.billingsoftware.entity.ShiftEntity;
import com.zirocraft.billingsoftware.repository.ExpenseRepository;
import com.zirocraft.billingsoftware.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShiftServiceImpl {

    private final ShiftRepository shiftRepository;
    private final ExpenseRepository expenseRepository;

    public Optional<ShiftEntity> getCurrentShift(String userId) {
        List<ShiftEntity> shifts = shiftRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(userId) && "OPEN".equals(s.getStatus()))
                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .toList();
        return shifts.isEmpty() ? Optional.empty() : Optional.of(shifts.get(0));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShiftEntity openShift(String userId, BigDecimal openingBalance) {
        // SOP: Tutup paksa shift lama yang menggantung & hitung saldonya (Agar tidak NULL)
        List<ShiftEntity> activeShifts = shiftRepository.findAll().stream()
                .filter(s -> s.getUserId().equals(userId) && "OPEN".equals(s.getStatus()))
                .toList();

        for(ShiftEntity s : activeShifts) {
            BigDecimal sales = s.getTotalSales() != null ? s.getTotalSales() : BigDecimal.ZERO;
            BigDecimal exp = s.getTotalExpenses() != null ? s.getTotalExpenses() : BigDecimal.ZERO;
            BigDecimal expected = s.getOpeningBalance().add(sales).subtract(exp);

            s.setStatus("CLOSED");
            s.setEndTime(new Timestamp(System.currentTimeMillis()));
            s.setExpectedBalance(expected);
            s.setActualBalance(BigDecimal.ZERO); // Kasir dianggap setor 0 jika kabur/lupa tutup
            s.setVariance(expected.negate());    // Selisih jadi minus sebesar uang yang seharusnya ada
            shiftRepository.save(s);
        }

        ShiftEntity newShift = ShiftEntity.builder()
                .userId(userId).openingBalance(openingBalance)
                .totalSales(BigDecimal.ZERO).totalExpenses(BigDecimal.ZERO).status("OPEN").build();
        return shiftRepository.save(newShift);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ExpenseEntity addExpense(Long shiftId, String description, BigDecimal amount, String userId) {
        ShiftEntity shift = shiftRepository.findById(shiftId).orElseThrow(() -> new RuntimeException("Shift not found"));
        if (!"OPEN".equals(shift.getStatus())) throw new RuntimeException("Shift closed");

        ExpenseEntity expense = ExpenseEntity.builder().shiftId(shiftId).description(description).amount(amount).userId(userId).build();
        expenseRepository.save(expense);

        BigDecimal currentExp = shift.getTotalExpenses() != null ? shift.getTotalExpenses() : BigDecimal.ZERO;
        shift.setTotalExpenses(currentExp.add(amount));
        shiftRepository.save(shift);
        return expense;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ShiftEntity closeShift(Long shiftId, BigDecimal actualPhysicalCash) {
        ShiftEntity shift = shiftRepository.findById(shiftId).orElseThrow(() -> new RuntimeException("Not found"));
        BigDecimal sales = shift.getTotalSales() != null ? shift.getTotalSales() : BigDecimal.ZERO;
        BigDecimal exp = shift.getTotalExpenses() != null ? shift.getTotalExpenses() : BigDecimal.ZERO;

        BigDecimal expected = shift.getOpeningBalance().add(sales).subtract(exp);
        shift.setExpectedBalance(expected);
        shift.setActualBalance(actualPhysicalCash);
        shift.setVariance(actualPhysicalCash.subtract(expected));
        shift.setEndTime(new Timestamp(System.currentTimeMillis()));
        shift.setStatus("CLOSED");
        return shiftRepository.save(shift);
    }

    public List<ShiftEntity> getClosedShifts() {
        return shiftRepository.findByStatusOrderByEndTimeDesc("CLOSED");
    }

    public List<ExpenseEntity> getAllExpenses() {
        return expenseRepository.findAll();
    }
}