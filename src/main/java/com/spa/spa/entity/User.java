package com.spa.spa.entity;

import java.time.LocalDateTime;

import com.spa.spa.dto.UserDto;
import com.spa.spa.enums.UserRole;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String name;

    private String phone;

    private UserRole role;

    private String verificationCode;
    private boolean verified = false;
    private LocalDateTime verificationExpiry;

    private String resetToken;
    private LocalDateTime resetTokenExpiry;

    public UserDto getDto(){
        UserDto userDto = new UserDto();
        userDto.setId(id);
        userDto.setName(name);
        userDto.setEmail(email);
        userDto.setRole(role);

        return userDto;
    }
}
