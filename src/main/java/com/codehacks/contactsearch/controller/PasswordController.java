package com.codehacks.contactsearch.controller;

import com.codehacks.contactsearch.dto.ChangePasswordRequest;
import com.codehacks.contactsearch.dto.ForgotPasswordRequest;
import com.codehacks.contactsearch.dto.ForcePasswordChangeRequest;
import com.codehacks.contactsearch.dto.ResetPasswordRequest;
import com.codehacks.contactsearch.service.PasswordResetService;
import com.codehacks.contactsearch.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Password Management", description = "Endpoints for password management operations")
public class PasswordController {

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/forgot")
    @Operation(summary = "Request password reset", description = "Send a password reset token to user's email")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            // Check if user exists with this email
            if (!userService.userExistsByEmail(request.getEmail())) {
                // Don't reveal if email exists or not for security
                log.info("Password reset requested for email: {}", request.getEmail());
                return ResponseEntity.ok(Map.of("message", "If the email exists, a reset token has been generated"));
            }

            // Generate reset token
            String resetToken = passwordResetService.generateResetToken(request.getEmail());
            
            // In a real application, you would send this token via email
            // For now, we'll log it (in production, remove this log)
            log.info("Password reset token generated for {}: {}", request.getEmail(), resetToken);
            
            return ResponseEntity.ok(Map.of(
                "message", "If the email exists, a reset token has been generated",
                "token", resetToken // Remove this in production - only for testing
            ));
            
        } catch (Exception e) {
            log.error("Error processing forgot password request for email: {}", request.getEmail(), e);
            return ResponseEntity.ok(Map.of("message", "If the email exists, a reset token has been generated"));
        }
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset password", description = "Reset password using reset token")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            // Validate reset token
            if (!passwordResetService.isValidResetToken(request.getResetToken())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired reset token"));
            }

            // Get email associated with token
            String email = passwordResetService.getEmailForToken(request.getResetToken());
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid reset token"));
            }

            // Reset password
            boolean success = userService.resetPassword(email, request.getNewPassword());
            if (!success) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to reset password"));
            }
            
            // Invalidate the token after use
            passwordResetService.invalidateToken(request.getResetToken());
            
            log.info("Password reset successful for email: {}", email);
            return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
            
        } catch (Exception e) {
            log.error("Error resetting password", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to reset password"));
        }
    }

    @PostMapping("/change")
    @Operation(summary = "Change password", description = "Change password when logged in (requires current password)")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            // Get current user from security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Change password
            boolean success = userService.changePassword(username, request.getCurrentPassword(), request.getNewPassword());
            if (!success) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to change password"));
            }
            
            log.info("Password changed successfully for user: {}", username);
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error changing password", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to change password"));
        }
    }

    @PostMapping("/force-change")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Force password change", description = "Admin endpoint to force password change for any user")
    public ResponseEntity<Map<String, String>> forcePasswordChange(@Valid @RequestBody ForcePasswordChangeRequest request) {
        try {
            // Check if user exists
            if (!userService.userExists(request.getUsername())) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            // Force password change
            boolean success = userService.forcePasswordChange(request.getUsername(), request.getNewPassword());
            if (!success) {
                return ResponseEntity.badRequest().body(Map.of("error", "Failed to change password"));
            }
            
            log.info("Password force changed for user: {} by admin", request.getUsername());
            return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
            
        } catch (Exception e) {
            log.error("Error force changing password for user: {}", request.getUsername(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to change password"));
        }
    }

    @GetMapping("/reset-token/validate")
    @Operation(summary = "Validate reset token", description = "Check if a reset token is valid")
    public ResponseEntity<Map<String, Object>> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.isValidResetToken(token);
        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        
        if (isValid) {
            response.put("message", "Token is valid");
        } else {
            response.put("message", "Token is invalid or expired");
        }
        
        return ResponseEntity.ok(response);
    }
} 