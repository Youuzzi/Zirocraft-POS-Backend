package com.zirocraft.billingsoftware.controller;

import com.zirocraft.billingsoftware.io.UserRequest;
import com.zirocraft.billingsoftware.io.UserResponse;
import com.zirocraft.billingsoftware.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;

    // 1. REGISTER: POST ke /api/v1.0/admin/users/register
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@RequestBody UserRequest request) {
        try {
            return userService.createUser(request);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gagal buat user: " + e.getMessage());
        }
    }

    // 2. READ ALL: GET ke /api/v1.0/admin/users
    @GetMapping
    public List<UserResponse> readUsers() {
        return userService.readUsers();
    }

    // 3. DELETE: DELETE ke /api/v1.0/admin/users/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String id) {
        try {
            userService.deleteUser(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User ID " + id + " tidak ditemukan");
        }
    }
}