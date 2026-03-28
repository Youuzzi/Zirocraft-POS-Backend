package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.SettingEntity;
import com.zirocraft.billingsoftware.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // IMPORT INI
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SettingServiceImpl {

    private final SettingRepository settingRepository;

    @Transactional // Wajib biar urusan simpan-menyimpan stabil
    public SettingEntity getSettings() {
        // Cari baris pertama (biasanya ID 1)
        return settingRepository.findAll().stream().findFirst().orElseGet(() -> {
            // Kalau BENER-BENER kosong, bikin baru TANPA set ID manual
            SettingEntity defaultSet = SettingEntity.builder()
                    .storeName("ZiroShop")
                    .defaultFloatAmount(new BigDecimal("50000"))
                    .currencySymbol("Rp")
                    .build();
            return settingRepository.save(defaultSet);
        });
    }

    @Transactional
    public SettingEntity updateSettings(SettingEntity newSettings) {
        // Ambil data lama dulu biar kita tau ID pastinya berapa
        SettingEntity existing = getSettings();

        // Update datanya saja, ID tetap pake yang lama
        existing.setStoreName(newSettings.getStoreName());
        existing.setDefaultFloatAmount(newSettings.getDefaultFloatAmount());

        return settingRepository.save(existing);
    }
}