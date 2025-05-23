package com.vehicle.authentication.inventory.controller;

import com.vehicle.authentication.inventory.authService.JwtUtil;
import com.vehicle.authentication.inventory.model.User;
import com.vehicle.authentication.inventory.repository.UserRepository;
import com.vehicle.authentication.inventory.service.UserDetailsServiceImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
//@CrossOrigin(origins = "http://inventory-management-client.s3-website.us-east-2.amazonaws.com")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final Set<String> tokenBlacklist = new HashSet<>();

    public AuthController(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtUtil.generateToken(String.valueOf(userDetails));

        return new LoginResponse(token, "Login successful");
    }

    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (tokenBlacklist.contains(token)) {
            throw new RuntimeException("Token is already blacklisted");
        }
        tokenBlacklist.add(token);
        return "Logout successful";
    }

    @GetMapping("/validate")
    public String validateToken(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (tokenBlacklist.contains(token)) {
            return "Token is invalid";
        }
        String username = jwtUtil.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtUtil.validateToken(token, userDetails) ? "Token is valid" : "Token is invalid";
    }

    @Data
    static class AuthenticationRequest {
        private String username;
        private String password;

        public AuthenticationRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "AuthenticationRequest{" +
                    "username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    '}';
        }
    }

    @Data
    static class LoginResponse {
        private String token;
        private String message;

        public LoginResponse(String token, String message) {
            this.token = token;
            this.message = message;
        }
    }

    @PostMapping("/register")
    public Map<String, String> registerUser(@RequestBody User user) {
        // Validate input
        if ((user.getUsername()).isEmpty() || (user.getPassword()).isEmpty()) {
            return Map.of("message", "Username and password cannot be null");
        }

        // Fetch users by username or email
        List<User> existingUsers = userRepository.findByUsernameOrEmail(user.getUsername(), user.getEmail());

        for (User foundUser : existingUsers) {
            if (foundUser.getUsername().equals(user.getUsername())) {
                return Map.of("message", "User already exists");
            }
            if (foundUser.getEmail().equals(user.getEmail())) {
                return Map.of("message", "Email already exists");
            }
        }

        // Encode the password and save the user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return Map.of("message", "User registered successfully");
    }

    // Get User By Username

    @GetMapping("/{username}")
    public User getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
}