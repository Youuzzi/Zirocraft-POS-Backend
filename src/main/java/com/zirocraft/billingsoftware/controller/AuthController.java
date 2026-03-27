package com.zirocraft.billingsoftware.controller;

import com.zirocraft.billingsoftware.io.AuthRequest;
import com.zirocraft.billingsoftware.io.AuthResponse;
import com.zirocraft.billingsoftware.service.UserService;
import com.zirocraft.billingsoftware.service.impl.AppUserDetailService;
import com.zirocraft.billingsoftware.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
// HAPUS @RequestMapping("/api/v1.0") di sini!
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AppUserDetailService appUserDetailService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login") // URL Final: http://localhost:8080/api/v1.0/login
    public AuthResponse login(@RequestBody AuthRequest request) throws Exception {
        authenticate(request.getEmail(), request.getPassword());
        final UserDetails userDetails = appUserDetailService.loadUserByUsername(request.getEmail());
        final String jwToken = jwtUtil.generateToken(userDetails);
        String role = userService.getUserRole(request.getEmail());
        return new AuthResponse(request.getEmail(), jwToken, role);
    }

    private void authenticate(String email, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email atau password salah, Zi!");
        }
    }
}