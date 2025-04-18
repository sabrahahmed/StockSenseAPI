package com.stocksense.service;

import com.stocksense.model.User;
import com.stocksense.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> registerUser(User user) {
        // Check if the email already exists
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email is already in use");
        }

        // Hash the password and save the user to the database
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        // Generate the JWT token
        String jwtToken = generateJwtToken(savedUser);

        // Create a response map containing the userId and token
        Map<String, Object> response = new HashMap<>();
        response.put("userId", savedUser.getId()); // Return the generated userId
        response.put("token", jwtToken); // Return the JWT token

        return response;
    }


    @Value("${jwt.secret}")
    private String jwtSecret;

    public Map<String, Object> loginUser(User loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail());
        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate the JWT token
        String jwtToken = generateJwtToken(user);

        // Create a response map containing the userId and token
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("token", jwtToken);

        return response;
    }

    public Map<String, Object> processGoogleLogin(User userData) {
        // Check if the user already exists by email
        User existingUser = userRepository.findByEmail(userData.getEmail());

        if (existingUser != null) {
            // If the user exists, return the existing user (no password needed)
            Map<String, Object> response = new HashMap<>();
            response.put("userId", existingUser.getId());

            return response;
        } else {
            // If the user doesn't exist, create a new user (no password needed)
            User newUser = new User();
            newUser.setName(userData.getName());
            newUser.setEmail(userData.getEmail());
            newUser.setPassword("google-login"); // Placeholder for password, can be null or any string

            User savedUser = userRepository.save(newUser);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", savedUser.getId());

            return response;
        }
    }

    private String generateJwtToken(User user) {
        // Convert jwtSecret string to SecretKey for signing
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 1 day expiration
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }
}
