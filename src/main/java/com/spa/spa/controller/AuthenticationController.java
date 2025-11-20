package com.spa.spa.controller;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

//import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CookieValue;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spa.spa.dto.AuthenticationRequest;
import com.spa.spa.dto.SignupRequestDTO;
import com.spa.spa.dto.UserDto;
import com.spa.spa.entity.User;
import com.spa.spa.repository.UserRepository;
import com.spa.spa.service.authentication.AuthService;
import com.spa.spa.service.jwt.UserDetailsServiceImpl;
import com.spa.spa.util.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

@RestController
//@CrossOrigin(origins = "http://localhost:5173")
public class AuthenticationController {
    @Autowired
    private AuthService authService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String HEADER_STRING = "Authorization";



    // @GetMapping("/login")
    // public String login() {
    //     return "Success";
    // }

    @PostMapping("/signup")
    public ResponseEntity<?> signupClient(@RequestBody SignupRequestDTO signupRequestDTO){
        if(authService.presentByEmail(signupRequestDTO.getEmail())){
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Email already exists. Please try logging in.");
            return new ResponseEntity<>("Client already exists with this Email", HttpStatus.NOT_ACCEPTABLE);
        }
        UserDto createdUser = authService.signupClient(signupRequestDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.OK);
    }


    @PostMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        
        if (code == null || code.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Verification code is required"));
        }
        
        User verifiedUser = authService.verifyToken(code);
        
        if (verifiedUser != null) {
            return ResponseEntity.ok(Map.of(
                "message", "Email verified successfully",
                "verified", true,
                "email", verifiedUser.getEmail()
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "message", "Invalid or expired verification code",
                    "verified", false
                ));
        }
    }
    

    @PostMapping("/create-password")
    public ResponseEntity<?> createPassword(
        @RequestBody Map<String, String> request,
        HttpServletResponse response
    ) {
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Email and password are required"));
        }

        User user = userRepository.findFirstByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "User not found"));
        }

        // hash password
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userRepository.save(user);
        
        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        // 🔹 Generate JWT using the same logic as your login endpoint
        String jwt = jwtUtil.generateToken(userDetails.getUsername(), user.getRole().name());
        // 🔹 Create HttpOnly cookie
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(true)       // true if HTTPS
                .sameSite("None")    // or "Lax" for same-origin setups
                .path("/")
                .maxAge(10 * 60 * 60) // 10 hours
                .build();

        // 🔹 Attach cookie to response
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // 🔹 Return response body
        return ResponseEntity.ok(Map.of(
            "message", "Password created successfully",
            "role", user.getRole().name(),
            "userId", user.getId()
        ));
    }


    @PostMapping({"/login"})
    public void createAuthenticationToken(@RequestBody 
        AuthenticationRequest authenticationRequest,
        HttpServletResponse response
    )throws IOException, JSONException{
        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getUsername(), 
                authenticationRequest.getPassword()
            ));
        }catch (BadCredentialsException e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Incorrect email or password\"}");
            return;
        }
        System.out.println("username or email: " + authenticationRequest.getUsername());

        
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        User user = userRepository.findFirstByEmail(authenticationRequest.getUsername());
        // 🔹 Generate JWT using the same logic as your login endpoint
        String jwt = jwtUtil.generateToken(userDetails.getUsername(), user.getRole().name());
        
        
        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
            .httpOnly(true)               // JS cannot access it
            .secure(true)                 // only sent over HTTPS (set to false for local dev if needed)
            .sameSite("None")           // prevents CSRF attacks
            .path("/")                    // send cookie on all paths
            .maxAge(10 * 60 * 60)         // 10 hours
            .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // Optional: send role or ID in JSON body (safe since no sensitive info)
        response.setContentType("application/json");
        response.getWriter().write(
            new JSONObject()
                .put("userId", user.getId())
                .put("role", user.getRole())
                .toString()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Overwrite the cookie with the same name and set max age to 0
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true) // use true in production (HTTPS)
                .path("/")
                .maxAge(0)
                .sameSite("None") // or "Lax" depending on your setup
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.forgotPassword(email);
        return ResponseEntity.ok("Password reset email sent successfully.");
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<String> resetPassword(@PathVariable String token, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password has been reset successfully.");
    }

    

    @GetMapping("/check-auth")
    public ResponseEntity<?> checkAuth(@CookieValue(value = "jwt", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Not logged in"));
        }

        try {
            // Extract username and role from the token
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractClaim(token, claims -> (String) claims.get("role"));

            if (jwtUtil.isTokenExpired(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "Token expired"));
            }

            // ✅ Valid token — user is logged in
            return ResponseEntity.ok(Map.of(
                "message", "Authenticated",
                "username", username,
                "role", role
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid token"));
        }
    }

}
