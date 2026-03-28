package com.zirocraft.billingsoftware.controller;

import com.zirocraft.billingsoftware.entity.SettingEntity;
import com.zirocraft.billingsoftware.service.impl.SettingServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/settings") // Path internal: /admin/settings
@RequiredArgsConstructor
public class SettingController {

    private final SettingServiceImpl settingService;

    @GetMapping
    public SettingEntity getSettings() {
        return settingService.getSettings();
    }

    @PutMapping
    public SettingEntity update(@RequestBody SettingEntity settings) {
        return settingService.updateSettings(settings);
    }
}