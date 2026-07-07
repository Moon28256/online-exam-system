package com.exam.backend.dto;

import lombok.Data;

@Data
public class RegisterDTO {
    private String username;
    private String password;
    private String role;
    private String realName;
    private String email;
}
