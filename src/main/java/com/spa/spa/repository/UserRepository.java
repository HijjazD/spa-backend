package com.spa.spa.repository;

//import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.spa.spa.entity.User;

public interface UserRepository extends JpaRepository<User, Long>{
    User findFirstByEmail(String email);
    User findFirstByVerificationCode(String code);
    User findFirstByResetToken(String resetToken);
    //Optional<User> findByUsername(String username); 
}
