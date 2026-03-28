package com.zirocraft.billingsoftware.controller;

import com.zirocraft.billingsoftware.entity.ShiftEntity;
import com.zirocraft.billingsoftware.service.impl.ShiftServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftServiceImpl shiftService;

    // Ambil shift aktif (Buat check di Frontend)
    @GetMapping("/current/{userId}")
    public ShiftEntity getCurrent(@PathVariable String userId) {
        return shiftService.getCurrentShift(userId).orElse(null);
    }

    // Buka shift baru
    @PostMapping("/open")
    public ShiftEntity open(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        java.math.BigDecimal amount = new java.math.BigDecimal(payload.get("openingBalance").toString());
        return shiftService.openShift(userId, amount);
    }
}