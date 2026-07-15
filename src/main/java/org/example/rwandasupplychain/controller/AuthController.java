package org.example.rwandasupplychain.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.rwandasupplychain.context.TenantContext;
import org.example.rwandasupplychain.dto.JwtResponse;
import org.example.rwandasupplychain.dto.LoginRequest;
import org.example.rwandasupplychain.dto.RegisterRequest;
import org.example.rwandasupplychain.Entities.Role;
import org.example.rwandasupplychain.Entities.Users;
import org.example.rwandasupplychain.repository.UserRepository;
import org.example.rwandasupplychain.service.JwtService;
import org.example.rwandasupplychain.service.TenantSchemaService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TenantSchemaService tenantSchemaService;

    @Value("${app.tenant.default-id:tenant1}")
    private String defaultTenantId;

    private String resolveTenantId(String provided) {
        return (provided == null || provided.isBlank()) ? defaultTenantId : provided;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        String tenantId = resolveTenantId(request.getTenantId());
        TenantContext.setTenantId(tenantId);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            Users user = (Users) authentication.getPrincipal();

            List<String> roles = user.getRoles().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());

            String token = jwtService.generateToken(
                    user.getUsername(),
                    tenantId,
                    roles
            );

            return ResponseEntity.ok(new JwtResponse(
                    token,
                    user.getUsername(),
                    roles,
                    tenantId
            ));

        } finally {
            TenantContext.clear();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String tenantId = resolveTenantId(request.getTenantId());
        TenantContext.setTenantId(tenantId);

        try {
            tenantSchemaService.createTenantSchema(tenantId);

            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("Username already exists");
            }

            String encodedPassword = passwordEncoder.encode(request.getPassword());

            Users user = new Users();
            user.setUsername(request.getUsername());
            user.setPassword(encodedPassword);
            user.setFullName(request.getFullName());
            user.setEmail(request.getEmail());

            List<Role> roleList = request.getRoles().stream()
                    .map(roleStr -> {
                        try {
                            return Role.valueOf(roleStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new RuntimeException("Invalid role: " + roleStr);
                        }
                    })
                    .collect(Collectors.toList());
            user.setRoles(roleList);
            user.setEnabled(true);

            userRepository.save(user);

            return ResponseEntity.ok("User registered successfully for tenant: " + tenantId);

        } finally {
            TenantContext.clear();
        }
    }
}