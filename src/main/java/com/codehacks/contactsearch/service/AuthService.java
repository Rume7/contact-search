package com.codehacks.contactsearch.service;

import com.codehacks.contactsearch.model.AuthRequest;
import com.codehacks.contactsearch.model.AuthResponse;
import com.codehacks.contactsearch.model.RegisterRequest;
import com.codehacks.contactsearch.model.Role;
import com.codehacks.contactsearch.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userService.userExists(request.username())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userService.userExistsByEmail(request.email())) {
            throw new RuntimeException("Email already exists");
        }

        User savedUser = userService.createUser(
            request.username(),
            request.email(),
            request.password(),
            request.firstName(),
            request.lastName(),
            Role.USER // Default role for new users
        );

        // Generate tokens
        String token = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        return AuthResponse.of(
            token,
            refreshToken,
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getRole(),
            jwtService.getJwtExpiration() / 1000 // Convert to seconds
        );
    }

    public AuthResponse authenticate(AuthRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
            )
        );

        User user = userService.getUserByUsername(request.username());

        // Generate tokens
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.of(
            token,
            refreshToken,
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            jwtService.getJwtExpiration() / 1000 // Convert to seconds
        );
    }

    public AuthResponse refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);

        User user = userService.getUserByUsername(username);

        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // Generate new tokens
        String newToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.of(
            newToken,
            newRefreshToken,
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            jwtService.getJwtExpiration() / 1000 // Convert to seconds
        );
    }
} 