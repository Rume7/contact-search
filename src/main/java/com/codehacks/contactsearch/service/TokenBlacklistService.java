package com.codehacks.contactsearch.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class TokenBlacklistService {
    
    private final ConcurrentMap<String, Long> blacklistedTokens = new ConcurrentHashMap<>();
    
    /**
     * Add a token to the blacklist
     * @param token JWT token to blacklist
     * @param expirationTime Token expiration time in milliseconds
     */
    public void blacklistToken(String token, long expirationTime) {
        blacklistedTokens.put(token, expirationTime);
    }
    
    /**
     * Check if a token is blacklisted
     * @param token JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        Long expirationTime = blacklistedTokens.get(token);
        if (expirationTime == null) {
            return false;
        }
        
        // Remove expired tokens
        if (System.currentTimeMillis() > expirationTime) {
            blacklistedTokens.remove(token);
            return false;
        }
        
        return true;
    }
    
    /**
     * Clean up expired tokens from blacklist
     */
    public void cleanupExpiredTokens() {
        long currentTime = System.currentTimeMillis();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue() < currentTime);
    }
} 