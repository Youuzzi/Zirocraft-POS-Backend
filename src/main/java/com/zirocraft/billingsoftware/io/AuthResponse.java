package com.zirocraft.billingsoftware.io;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String email;
    private String token;
    private String role;
    private String name;
}