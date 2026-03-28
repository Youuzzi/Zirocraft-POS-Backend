package com.zirocraft.billingsoftware.repository;

import com.zirocraft.billingsoftware.entity.SettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<SettingEntity, Long> {
}