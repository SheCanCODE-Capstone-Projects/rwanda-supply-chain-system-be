package org.example.rwandasupplychain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.example.rwandasupplychain.Entities.Users;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<Users, UUID> {
    Optional<Users> findByUsername(String username);
}