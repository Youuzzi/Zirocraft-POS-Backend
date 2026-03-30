package com.zirocraft.billingsoftware.service.impl;

import com.zirocraft.billingsoftware.entity.UserEntity;
import com.zirocraft.billingsoftware.io.UserRequest;
import com.zirocraft.billingsoftware.io.UserResponse;
import com.zirocraft.billingsoftware.repository.UserRepository;
import com.zirocraft.billingsoftware.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request) {
        // PROTEKSI: Cek Email
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email sudah terdaftar!");
        }

        UserEntity newUser = UserEntity.builder()
                .userId(UUID.randomUUID().toString())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_" + request.getRole().toUpperCase())
                .name(request.getName())
                .build();

        newUser = userRepository.save(newUser);
        return convertToResponse(newUser);
    }

    private UserResponse convertToResponse(UserEntity user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override public String getUserRole(String email) { return userRepository.findByEmail(email).get().getRole(); }
    @Override public List<UserResponse> readUsers() { return userRepository.findAll().stream().map(this::convertToResponse).collect(Collectors.toList()); }
    @Override public void deleteUser(String id) { UserEntity user = userRepository.findByUserId(id).get(); userRepository.delete(user); }
}