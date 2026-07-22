package org.example.rwandasupplychain.Entities.InvetoryManagementEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    

}
