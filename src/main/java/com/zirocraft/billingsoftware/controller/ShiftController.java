package com.zirocraft.billingsoftware.controller;

import com.zirocraft.billingsoftware.entity.ShiftEntity;
import com.zirocraft.billingsoftware.service.impl.ShiftServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftServiceImpl shiftService;

    @GetMapping("/current/{userId}")
    public ShiftEntity getCurrent(@PathVariable String userId) {
        return shiftService.getCurrentShift(userId).orElse(null);
    }

    @PostMapping("/open")
    public ShiftEntity open(@RequestBody Map<String, Object> payload) {
        String userId = payload.get("userId").toString();
        BigDecimal amount = new BigDecimal(payload.get("openingBalance").toString());
        return shiftService.openShift(userId, amount);
    }

    @PostMapping("/close")
    public ShiftEntity close(@RequestBody Map<String, Object> payload) {
        // Ambil data dengan lebih aman dari Map
        Long shiftId = Long.valueOf(payload.get("shiftId").toString());
        BigDecimal actualCash = new BigDecimal(payload.get("actualBalance").toString());
        return shiftService.closeShift(shiftId, actualCash);
    }
}