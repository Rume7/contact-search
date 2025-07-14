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
} 