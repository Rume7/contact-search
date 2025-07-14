package com.codehacks.contactsearch.service;

import com.codehacks.contactsearch.model.Role;
import com.codehacks.contactsearch.model.User;
import com.codehacks.contactsearch.model.UserProfileResponse;
import com.codehacks.contactsearch.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

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
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testGetUserByUsername_UserFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User found = userService.getUserByUsername("testuser");

        assertThat(found).isEqualTo(user);
    }

    @Test
    void testGetUserByUsername_UserNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: missing");
    }

    @Test
    void testCreateUser_ShouldEncodePasswordAndSave() {
        String rawPassword = "plainPassword";
        String encodedPassword = "encodedPassword";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = userService.createUser(
                "newuser", "new@example.com", rawPassword, "New", "User", Role.ADMIN);

        assertThat(created.getUsername()).isEqualTo("newuser");
        assertThat(created.getEmail()).isEqualTo("new@example.com");
        assertThat(created.getPassword()).isEqualTo(encodedPassword);
        assertThat(created.getFirstName()).isEqualTo("New");
        assertThat(created.getLastName()).isEqualTo("User");
        assertThat(created.getRole()).isEqualTo(Role.ADMIN);

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testGetUserProfile_UserFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserProfileResponse profile = userService.getUserProfile("testuser");

        assertThat(profile.username()).isEqualTo(user.getUsername());
        assertThat(profile.email()).isEqualTo(user.getEmail());
        assertThat(profile.firstName()).isEqualTo(user.getFirstName());
        assertThat(profile.lastName()).isEqualTo(user.getLastName());
        assertThat(profile.role()).isEqualTo(user.getRole());
        assertThat(profile.createdAt()).isEqualTo(user.getCreatedAt());
        assertThat(profile.updatedAt()).isEqualTo(user.getUpdatedAt());
    }

    @Test
    void testGetUserProfile_UserNotFound() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfile("missing"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found: missing");
    }

    @Test
    void testUserExists_ShouldReturnTrue() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThat(userService.userExists("testuser")).isTrue();
    }

    @Test
    void testUserExists_ShouldReturnFalse() {
        when(userRepository.existsByUsername("nouser")).thenReturn(false);

        assertThat(userService.userExists("nouser")).isFalse();
    }

    @Test
    void testUserExistsByEmail_ShouldReturnTrue() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThat(userService.userExistsByEmail("test@example.com")).isTrue();
    }

    @Test
    void testUserExistsByEmail_ShouldReturnFalse() {
        when(userRepository.existsByEmail("noemail@example.com")).thenReturn(false);

        assertThat(userService.userExistsByEmail("noemail@example.com")).isFalse();
    }
} 