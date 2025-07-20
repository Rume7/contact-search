package com.codehacks.contactsearch.service;

import com.codehacks.contactsearch.model.Role;
import com.codehacks.contactsearch.model.User;
import com.codehacks.contactsearch.model.UserProfileResponse;
import com.codehacks.contactsearch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get user by username
     * @param username Username to find
     * @return User entity
     * @throws UsernameNotFoundException if user not found
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Get user by email
     * @param email Email to find
     * @return User entity
     * @throws UsernameNotFoundException if user not found
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    /**
     * Create a new user
     * @param username Username for the new user
     * @param email Email for the new user
     * @param password Plain text password (will be encoded)
     * @param firstName User's first name
     * @param lastName User's last name
     * @param role User's role
     * @return The created User entity
     */
    public User createUser(String username, String email, String password, 
                          String firstName, String lastName, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        
        return userRepository.save(user);
    }

    /**
     * Get user profile by username
     * @param username Username to find
     * @return UserProfileResponse with user information
     * @throws UsernameNotFoundException if user not found
     */
    public UserProfileResponse getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return UserProfileResponse.of(
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }

    /**
     * Check if user exists by username
     * @param username Username to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if user exists by email
     * @param email Email to check
     * @return true if user exists, false otherwise
     */
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Change password for a user (requires current password verification)
     * @param username Username of the user
     * @param currentPassword Current password for verification
     * @param newPassword New password to set
     * @return true if password changed successfully
     * @throws IllegalArgumentException if current password is incorrect
     */
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        User user = getUserByUsername(username);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }

    /**
     * Reset password using reset token (for forgot password flow)
     * @param email Email of the user
     * @param newPassword New password to set
     * @return true if password reset successfully
     */
    public boolean resetPassword(String email, String newPassword) {
        User user = getUserByEmail(email);
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }

    /**
     * Force password change (admin function - no current password required)
     * @param username Username of the user
     * @param newPassword New password to set
     * @return true if password changed successfully
     */
    public boolean forcePasswordChange(String username, String newPassword) {
        User user = getUserByUsername(username);
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }
} 