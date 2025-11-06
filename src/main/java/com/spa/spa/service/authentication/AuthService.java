package com.spa.spa.service.authentication;

import com.spa.spa.dto.SignupRequestDTO;
import com.spa.spa.dto.UserDto;
import com.spa.spa.entity.User;

public interface AuthService {
    UserDto signupClient(SignupRequestDTO signupRequestDTO);
    Boolean presentByEmail(String email);
    User verifyToken(String code);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
