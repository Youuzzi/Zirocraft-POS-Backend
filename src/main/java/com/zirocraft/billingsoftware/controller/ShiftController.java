package com.zirocraft.billingsoftware.controller;

import com.zirocraft.billingsoftware.entity.ExpenseEntity;
import com.zirocraft.billingsoftware.entity.ShiftEntity;
import com.zirocraft.billingsoftware.service.impl.ShiftServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftServiceImpl shiftService;

    @GetMapping("/current/{userId}")
    public ShiftEntity getCurrent(@PathVariable String userId, Principal principal) {
        if (!userId.equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return shiftService.getCurrentShift(userId).orElse(null);
    }

    @PostMapping("/open")
    public ShiftEntity open(@RequestBody Map<String, Object> payload) {
        String userId = payload.get("userId").toString();
        BigDecimal amount = new BigDecimal(payload.get("openingBalance").toString());
        return shiftService.openShift(userId, amount);
    }

    @PostMapping("/expense")
    public ExpenseEntity addExpense(@RequestBody Map<String, Object> payload) {
        Long shiftId = Long.valueOf(payload.get("shiftId").toString());
        String desc = payload.get("description").toString();
        BigDecimal amount = new BigDecimal(payload.get("amount").toString());
        String userId = payload.get("userId").toString();

        return shiftService.addExpense(shiftId, desc, amount, userId);
    }

    @PostMapping("/close")
    public ShiftEntity close(@RequestBody Map<String, Object> payload) {
        Long shiftId = Long.valueOf(payload.get("shiftId").toString());
        BigDecimal actualCash = new BigDecimal(payload.get("actualBalance").toString());
        return shiftService.closeShift(shiftId, actualCash);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<ShiftEntity> getShiftHistory() {
        return shiftService.getClosedShifts();
    }

    // --- LOGIC BARU: AMBIL SEMUA PENGELUARAN (KHUSUS ADMIN) ---
    @GetMapping("/expenses/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<ExpenseEntity> getAllExpenses() {
        return shiftService.getAllExpenses();
    }
}