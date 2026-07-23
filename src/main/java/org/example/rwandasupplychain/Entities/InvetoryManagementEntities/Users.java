package org.example.rwandasupplychain.Entities.InvetoryManagementEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.rwandasupplychain.Enums.BusinessType;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    private String fullName;

    private String email;

    private String phone;

    private String password;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    private Date birthDate;


}
