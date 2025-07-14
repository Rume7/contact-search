package com.codehacks.contactsearch.service;

import com.codehacks.contactsearch.model.AuthRequest;
import com.codehacks.contactsearch.model.AuthResponse;
import com.codehacks.contactsearch.model.RegisterRequest;
import com.codehacks.contactsearch.model.Role;
import com.codehacks.contactsearch.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User user;
    private RegisterRequest registerRequest;
    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);

        registerRequest = new RegisterRequest(
                "testuser", "test@example.com", "password", "Test", "User"
        );
        authRequest = new AuthRequest("testuser", "password");
    }

    @Test
    void testRegister_Success() {
        when(userService.userExists("testuser")).thenReturn(false);
        when(userService.userExistsByEmail("test@example.com")).thenReturn(false);
        when(userService.createUser(eq("testuser"), eq("test@example.com"), eq("password"), eq("Test"), eq("User"), eq(Role.USER)))
                .thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.expiresIn()).isEqualTo(3600L);
    }

    @Test
    void testRegister_UsernameExists() {
        when(userService.userExists("testuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already exists");
    }

    @Test
    void testRegister_EmailExists() {
        when(userService.userExists("testuser")).thenReturn(false);
        when(userService.userExistsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void testAuthenticate_Success() {
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);

        // authenticationManager.authenticate should be called with correct token
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        AuthResponse response = authService.authenticate(authRequest);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.expiresIn()).isEqualTo(3600L);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testRefreshToken_Success() {
        String refreshToken = "refresh-token";

        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("new-refresh-token");
        when(jwtService.getJwtExpiration()).thenReturn(3600000L);

        AuthResponse response = authService.refreshToken(refreshToken);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.username()).isEqualTo("testuser");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.role()).isEqualTo(Role.USER);
        assertThat(response.expiresIn()).isEqualTo(3600L);
    }

    @Test
    void testRefreshToken_InvalidToken() {
        String refreshToken = "refresh-token";

        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        when(userService.getUserByUsername("testuser")).thenReturn(user);
        when(jwtService.isTokenValid(refreshToken, user)).thenReturn(false);

        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid refresh token");
    }
} 