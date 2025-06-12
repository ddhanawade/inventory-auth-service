package com.vehicle.authentication.inventory.controller;

import com.vehicle.authentication.inventory.authService.JwtUtil;
import com.vehicle.authentication.inventory.model.User;
import com.vehicle.authentication.inventory.repository.UserRepository;
import com.vehicle.authentication.inventory.service.EmailService;
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
//@CrossOrigin(origins = "http://vehicle-inventory-client.s3-website.us-east-2.amazonaws.com")
//@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

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

    @PostMapping("/forget-password")
    public Map<String, String> forgetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return Map.of("message", "Email not found");
        }
        // Generate a reset token
        String resetToken = UUID.randomUUID().toString();
        // Save the reset token in the database (or a separate field)
        User user = userOptional.get();
        user.setResetToken(resetToken); // Add a resetToken field in the User entity
        user.setTokenExpiry(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // 15 minutes expiry
        userRepository.save(user);
        // Send the reset token to the user's email
        String resetLink = "http://localhost:4200/#/reset-password?token=" + resetToken;
        String subject = "Password Reset Request for Your Fleet Manager Account";
        String body = "Dear " + user.getUsername() + ",\n\n" +
                "We received a request to reset the password for your account associated with this email address. " +
                "If you made this request, please click the link below to reset your password:\n\n" +
                resetLink + "\n\n" +
                "This link will expire in 15 minutes. If you did not request a password reset, please ignore this email or contact our support team if you have concerns.\n\n" +
                "For your security, please do not share this email or the reset link with anyone.\n\n" +
                "Thank you,\n" +
                "The Fleet Manager Team\n\n" +
                "Note: This is an automated email. Please do not reply to this message.";
        emailService.sendEmail(email, subject, body);

        return Map.of("message", "Password reset link sent to your email");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@RequestBody Map<String, String> request) {
        String resetToken = request.get("token");
        String newPassword = request.get("newPassword");

        // Validate input
        if (resetToken == null || resetToken.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return Map.of("message", "Token and new password cannot be null or empty");
        }

        // Find user by reset token
        Optional<User> userOptional = userRepository.findByResetToken(resetToken);

        if (userOptional.isEmpty()) {
            return Map.of("message", "Invalid or expired reset token");
        }

        User user = userOptional.get();

        // Check if the token is expired
        if (user.getTokenExpiry() == null || user.getTokenExpiry().before(new Date())) {
            return Map.of("message", "Reset token has expired");
        }

        // Update the password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Clear the reset token
        user.setTokenExpiry(null); // Clear the expiry
        userRepository.save(user);

        return Map.of("message", "Password reset successful");
    }


}