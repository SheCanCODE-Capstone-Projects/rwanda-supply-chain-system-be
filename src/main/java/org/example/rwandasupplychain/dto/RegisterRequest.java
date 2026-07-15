package org.example.rwandasupplychain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class RegisterRequest {
    private String tenantId;          // now optional

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private String fullName;
    private String email;
    private List<String> roles;
}