package com.spa.spa.service.authentication;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.spa.spa.dto.SignupRequestDTO;
import com.spa.spa.dto.UserDto;
import com.spa.spa.entity.User;
import com.spa.spa.enums.UserRole;
import com.spa.spa.repository.UserRepository;
import com.spa.spa.service.mail.MailService;

@Service
public class AuthServiceImpl implements AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    public Boolean presentByEmail(String email){
        return userRepository.findFirstByEmail(email) != null;
    }

    public UserDto signupClient(SignupRequestDTO signupRequestDTO){
        User user = new User();

        user.setEmail(signupRequestDTO.getEmail());
        user.setName(signupRequestDTO.getName());
        user.setPhone(signupRequestDTO.getPhone());
        user.setRole(UserRole.CLIENT);

        String code = String.format("%06d", new Random().nextInt(999999));
        user.setVerificationCode(code);
        user.setVerificationExpiry(LocalDateTime.now().plusMinutes(10)); // expires in 10 mins


        userRepository.save(user);

        String subject = "Verify your email address";
        String body = "Hello,\n\n"
                + "Thank you for registering! Your verification code is: " + code + "\n"
                + "This code will expire in 10 minutes.\n\n"
                + "Best regards,\nSPA System Team";
        mailService.sendMail(user.getEmail(), subject, body);

        return user.getDto(); 
    }

    public User verifyToken(String code){
        User user = userRepository.findFirstByVerificationCode(code);

        if(user == null){
            return null;
        }

        if(user.getVerificationExpiry().isBefore(LocalDateTime.now())){
            return null;
        }

        user.setVerificationCode(null); // clear the code after successful verification
        user.setVerificationExpiry(null);
        user.setVerified(true); // mark user as verified (assuming you have this field)
        userRepository.save(user);

        return user;
    }

    public void forgotPassword(String email) {
        User user = userRepository.findFirstByEmail(email);

        if (user == null) {
            throw new RuntimeException("No user found with that email address.");
        }

        // Generate reset token (random and unique)
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15)); // token valid for 15 minutes

        userRepository.save(user);

        // Create reset link
        String resetLink = "http://http://localhost:5173/reset-password/" + token;

        // Send email
        String subject = "Password Reset Request";
        String body = "Hello " + user.getName() + ",\n\n"
                + "We received a request to reset your password.\n"
                + "You can reset your password by clicking the link below:\n\n"
                + resetLink + "\n\n"
                + "This link will expire in 15 minutes.\n"
                + "If you didn't request a password reset, please ignore this email.\n\n"
                + "Best regards,\nSPA System Team";

        mailService.sendMail(user.getEmail(), subject, body);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findFirstByResetToken(token);

        if (user == null) {
            throw new RuntimeException("Invalid password reset token.");
        }

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Password reset token has expired.");
        }

        // ✅ Encrypt the new password before saving
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear token fields after successful reset
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }


}
