package com.zirocraft.billingsoftware.controller;

import com.zirocraft.billingsoftware.entity.UserEntity;
import com.zirocraft.billingsoftware.io.AuthRequest;
import com.zirocraft.billingsoftware.io.AuthResponse;
import com.zirocraft.billingsoftware.repository.UserRepository;
import com.zirocraft.billingsoftware.service.impl.AppUserDetailService;
import com.zirocraft.billingsoftware.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // IMPORT INI WAJIB
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailService appUserDetailService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        // 1. Validasi Akun
        authenticate(request.getEmail(), request.getPassword());

        // 2. Generate Token
        final UserDetails userDetails = appUserDetailService.loadUserByUsername(request.getEmail());
        final String jwToken = jwtUtil.generateToken(userDetails);

        // 3. Ambil data User dari Database
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan"));

        // 4. Return menggunakan Builder Pattern
        return AuthResponse.builder()
                .email(user.getEmail())
                .token(jwToken)
                .role(user.getRole())
                .name(user.getName())
                .build();
    }

    private void authenticate(String email, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email atau password salah");
        }
    }
}