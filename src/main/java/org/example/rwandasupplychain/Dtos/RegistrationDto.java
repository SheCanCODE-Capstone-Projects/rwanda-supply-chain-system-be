package org.example.rwandasupplychain.Dtos;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.example.rwandasupplychain.Enums.BusinessType;

import java.util.Date;

@Getter
@Setter
public class RegistrationDto {

    private String fullName;

    private String email;

    private String phone;

    private String password;

    @Enumerated(EnumType.STRING)
    private BusinessType businessType;

    private Date birthDate;
}
