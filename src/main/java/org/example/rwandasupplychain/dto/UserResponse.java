package org.example.rwandasupplychain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.rwandasupplychain.Entities.Role;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private List<Role> roles;
    private boolean enabled;
}