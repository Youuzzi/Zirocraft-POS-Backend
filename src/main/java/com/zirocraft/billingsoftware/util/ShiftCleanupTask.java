package com.zirocraft.billingsoftware.util;

import com.zirocraft.billingsoftware.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ShiftCleanupTask {

    private final ShiftRepository shiftRepository;

    // Jalan otomatis setiap jam 12 malem (Cron Job)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoCloseExpiredShifts() {
        System.out.println("Zirocraft Studio: Memulai pembersihan shift kadaluarsa...");

        // Kita cari semua yang statusnya OPEN, kita paksa jadi CLOSED
        // (Bisa dikasih logika tambahan: hanya yang sudah lebih dari 24 jam)
        shiftRepository.findAll().stream()
                .filter(shift -> "OPEN".equals(shift.getStatus()))
                .forEach(shift -> {
                    shift.setStatus("CLOSED");
                    shiftRepository.save(shift);
                });

        System.out.println("Zirocraft Studio: Semua shift sudah ditutup otomatis.");
    }
}