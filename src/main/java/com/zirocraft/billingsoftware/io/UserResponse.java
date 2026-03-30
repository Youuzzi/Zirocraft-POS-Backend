package com.zirocraft.billingsoftware.io;

import lombok.*;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private String userId;
    private String name;
    private String email;
    private String role;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}