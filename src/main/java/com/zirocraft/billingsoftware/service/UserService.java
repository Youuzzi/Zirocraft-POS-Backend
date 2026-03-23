package com.zirocraft.billingsoftware.service;

import com.zirocraft.billingsoftware.io.UserRequest;
import com.zirocraft.billingsoftware.io.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);

    String getUserRole(String email);

    List<UserResponse> readUsers();

    void deleteUser(String id);
}
