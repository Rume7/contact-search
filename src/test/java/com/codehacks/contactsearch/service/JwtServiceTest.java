package com.codehacks.contactsearch.service;

import com.codehacks.contactsearch.model.Role;
import com.codehacks.contactsearch.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private JwtService jwtService;

    private User user;

    @BeforeEach
    void setUp() {
        // Set up test properties
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L); // 7 days

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testGenerateToken_ShouldCreateValidToken() {
        String token = jwtService.generateToken(user);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void testGenerateToken_WithExtraClaims_ShouldIncludeClaims() {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");
        
        String token = jwtService.generateToken(extraClaims, user);
        
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        
        // Extract and verify custom claim
        String customClaim = jwtService.extractClaim(token, claims -> claims.get("customClaim", String.class));
        assertThat(customClaim).isEqualTo("customValue");
    }

    @Test
    void testGenerateRefreshToken_ShouldCreateValidToken() {
        String refreshToken = jwtService.generateRefreshToken(user);
        
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertThat(refreshToken.split("\\.")).hasSize(3);
    }

    @Test
    void testExtractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(user);
        String username = jwtService.extractUsername(token);
        
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void testExtractClaim_ShouldReturnCorrectClaim() {
        String token = jwtService.generateToken(user);
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        
        assertThat(role).isEqualTo("USER");
    }

    @Test
    void testIsTokenValid_WithValidToken_ShouldReturnTrue() {
        when(tokenBlacklistService.isBlacklisted(any())).thenReturn(false);
        
        String token = jwtService.generateToken(user);
        boolean isValid = jwtService.isTokenValid(token, user);
        
        assertThat(isValid).isTrue();
        verify(tokenBlacklistService).isBlacklisted(token);
    }

    @Test
    void testIsTokenValid_WithBlacklistedToken_ShouldReturnFalse() {
        String token = jwtService.generateToken(user);
        when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);
        
        boolean isValid = jwtService.isTokenValid(token, user);
        
        assertThat(isValid).isFalse();
        verify(tokenBlacklistService).isBlacklisted(token);
    }

    @Test
    void testIsTokenValid_WithWrongUsername_ShouldReturnFalse() {
        when(tokenBlacklistService.isBlacklisted(any())).thenReturn(false);
        
        String token = jwtService.generateToken(user);
        
        User wrongUser = new User();
        wrongUser.setUsername("wronguser");
        
        boolean isValid = jwtService.isTokenValid(token, wrongUser);
        
        assertThat(isValid).isFalse();
    }

    @Test
    void testInvalidateToken_ShouldAddToBlacklist() {
        String token = jwtService.generateToken(user);
        
        jwtService.invalidateToken(token);
        
        verify(tokenBlacklistService).blacklistToken(eq(token), any(Long.class));
    }

    @Test
    void testGetJwtExpiration_ShouldReturnCorrectValue() {
        long expiration = jwtService.getJwtExpiration();
        
        assertThat(expiration).isEqualTo(3600000L);
    }

    @Test
    void testTokenContainsUserInformation() {
        String token = jwtService.generateToken(user);
        
        String role = jwtService.extractClaim(token, claims -> claims.get("role", String.class));
        String email = jwtService.extractClaim(token, claims -> claims.get("email", String.class));
        String firstName = jwtService.extractClaim(token, claims -> claims.get("firstName", String.class));
        String lastName = jwtService.extractClaim(token, claims -> claims.get("lastName", String.class));
        
        assertThat(role).isEqualTo("USER");
        assertThat(email).isEqualTo("test@example.com");
        assertThat(firstName).isEqualTo("Test");
        assertThat(lastName).isEqualTo("User");
    }

    @Test
    void testTokenExpiration_ShouldBeInFuture() {
        String token = jwtService.generateToken(user);
        
        java.util.Date expiration = jwtService.extractClaim(token, Claims::getExpiration);
        java.util.Date now = new java.util.Date();
        
        assertThat(expiration).isAfter(now);
    }

    @Test
    void testRefreshTokenExpiration_ShouldBeLongerThanJwtToken() {
        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        java.util.Date jwtExpiration = jwtService.extractClaim(jwtToken, Claims::getExpiration);
        java.util.Date refreshExpiration = jwtService.extractClaim(refreshToken, Claims::getExpiration);
        
        assertThat(refreshExpiration).isAfter(jwtExpiration);
    }

    @Test
    void testTokenIssuerAndAudience() {
        String token = jwtService.generateToken(user);
        
        String issuer = jwtService.extractClaim(token, Claims::getIssuer);
        String audience = jwtService.extractClaim(token, claims -> claims.getAudience().iterator().next());
        
        assertThat(issuer).isEqualTo("contact-search-app");
        assertThat(audience).isEqualTo("contact-search-users");
    }

    @Test
    void testExtractUsername_WithInvalidToken_ShouldThrowException() {
        assertThatThrownBy(() -> jwtService.extractUsername("invalid.token.here"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void testIsTokenValid_WithInvalidToken_ShouldReturnFalse() {
        when(tokenBlacklistService.isBlacklisted(any())).thenReturn(false);
        
        // The service should handle invalid tokens gracefully
        boolean isValid = jwtService.isTokenValid("invalid.token.here", user);
        
        assertThat(isValid).isFalse();
    }

    @Test
    void testGenerateToken_WithDifferentUserRoles() {
        user.setRole(Role.ADMIN);
        String adminToken = jwtService.generateToken(user);
        
        user.setRole(Role.MODERATOR);
        String moderatorToken = jwtService.generateToken(user);
        
        String adminRole = jwtService.extractClaim(adminToken, claims -> claims.get("role", String.class));
        String moderatorRole = jwtService.extractClaim(moderatorToken, claims -> claims.get("role", String.class));
        
        assertThat(adminRole).isEqualTo("ADMIN");
        assertThat(moderatorRole).isEqualTo("MODERATOR");
    }

    @Test
    void testTokenId_ShouldBeUnique() {
        String token1 = jwtService.generateToken(user);
        String token2 = jwtService.generateToken(user);
        
        String id1 = jwtService.extractClaim(token1, Claims::getId);
        String id2 = jwtService.extractClaim(token2, Claims::getId);
        
        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
    }
} 