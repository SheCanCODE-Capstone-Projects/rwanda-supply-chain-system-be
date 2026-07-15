package org.example.rwandasupplychain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    private String tenantId;          // now optional

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}