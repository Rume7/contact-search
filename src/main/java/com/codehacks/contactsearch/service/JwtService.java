package com.codehacks.contactsearch.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration:604800000}")
    private long refreshExpiration;

    private final TokenBlacklistService tokenBlacklistService;

    public JwtService(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        // Add standard and custom claims
        Map<String, Object> claims = new HashMap<>(extraClaims);
        if (userDetails instanceof com.codehacks.contactsearch.model.User user) {
            claims.put("role", user.getRole().name());
            claims.put("email", user.getEmail());
            claims.put("firstName", user.getFirstName());
            claims.put("lastName", user.getLastName());
        }
        return Jwts
                .builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .id(UUID.randomUUID().toString())
                .issuer("contact-search-app")
                .audience().add("contact-search-users").and()
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey()) // Use non-deprecated method
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            // Check if token is blacklisted
            if (tokenBlacklistService.isBlacklisted(token)) {
                return false;
            }
            
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (Exception e) {
            // Invalid token format or other parsing errors
            return false;
        }
    }

    public void invalidateToken(String token) {
        // Extract expiration time from token
        Date expiration = extractExpiration(token);
        long expirationTime = expiration.getTime();
        
        // Add to blacklist
        tokenBlacklistService.blacklistToken(token, expirationTime);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public long getJwtExpiration() {
        return jwtExpiration;
    }
} 