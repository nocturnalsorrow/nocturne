package com.danialrekhman.userservice.service;

import com.danialrekhman.userservice.kafka.UserEventProducer;
import com.danialrekhman.userservice.model.Role;
import com.danialrekhman.userservice.model.User;
import com.danialrekhman.userservice.model.VerificationToken;
import com.danialrekhman.userservice.exception.*;
import com.danialrekhman.userservice.repository.UserRepository;
import com.danialrekhman.userservice.security.JwtService;
import com.danialrekhman.userservice.security.MyUserDetails;
import com.danialrekhman.userservice.dto.UserUpdateRequestDTO;
import com.danialrekhman.commonevents.VerificationEmailEvent;
import com.danialrekhman.commonevents.UserRegisteredEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserEventProducer userEventProducer;

    @Mock
    private VerificationTokenService verificationTokenService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User adminUser;
    private Authentication userAuthentication;
    private Authentication adminAuthentication;
    private VerificationToken verificationToken;

    @BeforeEach
    void setUp() {
        // Default user
        testUser = User.builder()
                .email("test@example.com")
                .username("testuser")
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .verified(false)
                .build();

        // Admin
        adminUser = User.builder()
                .email("admin@example.com")
                .username("admin")
                .password("encodedPassword")
                .role(Role.ROLE_ADMIN)
                .verified(true)
                .build();

        // It's prevents UnnecessaryStubbingException.
        userAuthentication = mock(Authentication.class);
        adminAuthentication = mock(Authentication.class);

        // Verification token
        verificationToken = VerificationToken.builder()
                .id(1L)
                .token("test-token-123")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
    }

    @Test
    @DisplayName("getAllUsers - successful for admin")
    void getAllUsers_AsAdmin_Success() {
        List<GrantedAuthority> adminAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(adminAuthentication.getAuthorities()).thenReturn((Collection) adminAuthorities);

        List<User> users = Arrays.asList(testUser, adminUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers(adminAuthentication);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllUsers - access denied for default user")
    void getAllUsers_AsUser_ThrowsAccessDeniedException() {
        List<GrantedAuthority> userAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(userAuthentication.getAuthorities()).thenReturn((Collection) userAuthorities);

        CustomAccessDeniedException exception = assertThrows(
                CustomAccessDeniedException.class,
                () -> userService.getAllUsers(userAuthentication));

        assertEquals("Only admin can see all users.", exception.getMessage());
        verify(userRepository, never()).findAll();
    }

    @Test
    @DisplayName("getUserByEmail - user will get his own profile")
    void getUserByEmail_OwnProfile_Success() {
        // Заглушки для имени и роли пользователя
        when(userAuthentication.getName()).thenReturn("test@example.com");
        List<GrantedAuthority> userAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(userAuthentication.getAuthorities()).thenReturn((Collection) userAuthorities);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        User result = userService.getUserByEmail("test@example.com", userAuthentication);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("getUserByEmail - admin will get other user profile")
    void getUserByEmail_AdminViewsOther_Success() {
        List<GrantedAuthority> adminAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(adminAuthentication.getAuthorities()).thenReturn((Collection) adminAuthorities);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        User result = userService.getUserByEmail("test@example.com", adminAuthentication);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    @DisplayName("getUserByEmail - default user cannot get other user profile")
    void getUserByEmail_UserViewsOther_ThrowsAccessDeniedException() {
        when(userAuthentication.getName()).thenReturn("test@example.com");
        List<GrantedAuthority> userAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(userAuthentication.getAuthorities()).thenReturn((Collection) userAuthorities);

        CustomAccessDeniedException exception = assertThrows(
                CustomAccessDeniedException.class,
                () -> userService.getUserByEmail("other@example.com", userAuthentication)
        );

        assertEquals("You can only view your own profile.", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("getUserByEmail - user not found")
    void getUserByEmail_UserNotFound_ThrowsException() {
        List<GrantedAuthority> adminAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(adminAuthentication.getAuthorities()).thenReturn((Collection) adminAuthorities);

        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserByEmail("nonexistent@example.com", adminAuthentication)
        );

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("signUpUser - successful sign up")
    void signUpUser_Success() {
        User newUser = User.builder()
                .email("newuser@example.com")
                .username("newuser")
                .password("plainPassword")
                .build();

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(verificationTokenService.createTokenForUser(any(User.class)))
                .thenReturn(verificationToken);

        User result = userService.signUpUser(newUser);

        assertNotNull(result);
        assertEquals(Role.ROLE_USER, result.getRole());
        assertFalse(result.isVerified());
        verify(passwordEncoder, times(1)).encode("plainPassword");
        verify(userRepository, times(1)).save(any(User.class));
        verify(verificationTokenService, times(1)).createTokenForUser(any(User.class));
        verify(userEventProducer, times(1)).publishVerificationEmail(any(VerificationEmailEvent.class));
    }

    @Test
    @DisplayName("signUpUser - user with this email already exist")
    void signUpUser_EmailExists_ThrowsException() {
        User newUser = User.builder()
                .email("test@example.com")
                .username("testuser")
                .password("password")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> userService.signUpUser(newUser)
        );

        assertTrue(exception.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any(User.class));
        verify(userEventProducer, never()).publishVerificationEmail(any());
    }

    @Test
    @DisplayName("verifyUser - successful verification")
    void verifyUser_Success() {
        when(verificationTokenService.findByToken("test-token-123"))
                .thenReturn(Optional.of(verificationToken));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.verifyUser("test-token-123"));

        assertTrue(testUser.isVerified());
        verify(userRepository, times(1)).save(testUser);
        verify(verificationTokenService, times(1)).deleteToken("test-token-123");
        verify(userEventProducer, times(1)).publishUserRegistered(any(UserRegisteredEvent.class));
    }

    @Test
    @DisplayName("verifyUser - invalid token")
    void verifyUser_InvalidToken_ThrowsException() {
        when(verificationTokenService.findByToken("invalid-token"))
                .thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(
                InvalidTokenException.class,
                () -> userService.verifyUser("invalid-token")
        );

        assertEquals("Invalid verification token", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(userEventProducer, never()).publishUserRegistered(any());
    }

    @Test
    @DisplayName("verifyUser - expired token")
    void verifyUser_ExpiredToken_ThrowsException() {
        VerificationToken expiredToken = VerificationToken.builder()
                .id(1L)
                .token("expired-token")
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusHours(1))
                .build();

        when(verificationTokenService.findByToken("expired-token"))
                .thenReturn(Optional.of(expiredToken));

        TokenExpiredException exception = assertThrows(
                TokenExpiredException.class,
                () -> userService.verifyUser("expired-token")
        );

        assertEquals("Verification token expired", exception.getMessage());
        verify(verificationTokenService, times(1)).deleteToken("expired-token");
        verify(userRepository, never()).save(any(User.class));
        verify(userEventProducer, never()).publishUserRegistered(any());
    }

    @Test
    @DisplayName("login - successful login")
    void verifyAndReturnToken_Success() {
        User verifiedUser = User.builder()
                .email("test@example.com")
                .username("testuser")
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .verified(true)
                .build();

        User credentials = new User();
        credentials.setEmail("test@example.com");
        credentials.setPassword("plainPassword");

        MyUserDetails userDetails = new MyUserDetails(verifiedUser);
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, "plainPassword", userDetails.getAuthorities()
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtService.generateToken("test@example.com", "ROLE_USER"))
                .thenReturn("jwt-token");

        String token = userService.verifyAndReturnToken(credentials);

        assertNotNull(token);
        assertEquals("jwt-token", token);
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).generateToken("test@example.com", "ROLE_USER");
    }

    @Test
    @DisplayName("login - user not verified")
    void verifyAndReturnToken_UserNotVerified_ThrowsException() {
        User credentials = new User();
        credentials.setEmail("test@example.com");
        credentials.setPassword("plainPassword");

        MyUserDetails userDetails = new MyUserDetails(testUser); // testUser не верифицирован
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, "plainPassword", userDetails.getAuthorities()
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);

        UserNotVerifiedException exception = assertThrows(
                UserNotVerifiedException.class,
                () -> userService.verifyAndReturnToken(credentials)
        );

        assertEquals("User not verified.", exception.getMessage());
        verify(jwtService, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("login - invalid user data")
    void verifyAndReturnToken_BadCredentials_ThrowsException() {
        User credentials = new User();
        credentials.setEmail("test@example.com");
        credentials.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        AuthenticationFailedException exception = assertThrows(
                AuthenticationFailedException.class,
                () -> userService.verifyAndReturnToken(credentials)
        );

        assertEquals("Invalid username or password.", exception.getMessage());
    }

    @Test
    @DisplayName("updateUser - user update his profile")
    void updateUser_OwnProfile_Success() {
        when(userAuthentication.getName()).thenReturn("test@example.com");
        List<GrantedAuthority> userAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(userAuthentication.getAuthorities()).thenReturn((Collection) userAuthorities);

        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                .username("updatedUsername")
                .password("newPassword")
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser("test@example.com", updateDTO, userAuthentication);

        assertNotNull(result);
        assertEquals("updatedUsername", result.getUsername());
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("updateUser - admin update other profile")
    void updateUser_AdminUpdatesOther_Success() {
        List<GrantedAuthority> adminAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(adminAuthentication.getAuthorities()).thenReturn((Collection) adminAuthorities);

        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                .username("adminUpdated")
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser("test@example.com", updateDTO, adminAuthentication);

        assertNotNull(result);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("updateUser - user trying update other profile")
    void updateUser_UserUpdatesOther_ThrowsAccessDeniedException() {
        when(userAuthentication.getName()).thenReturn("test@example.com");
        List<GrantedAuthority> userAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(userAuthentication.getAuthorities()).thenReturn((Collection) userAuthorities);

        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                .username("hacked")
                .build();

        User otherUser = User.builder()
                .email("other@example.com")
                .username("other")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(otherUser));

        CustomAccessDeniedException exception = assertThrows(
                CustomAccessDeniedException.class,
                () -> userService.updateUser("other@example.com", updateDTO, userAuthentication)
        );

        assertEquals("You can only update your own profile.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateUser - user not found")
    void updateUser_UserNotFound_ThrowsException() {
        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder().build();

        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser("nonexistent@example.com", updateDTO, adminAuthentication)
        );
    }

    @Test
    @DisplayName("updateUser - updating username only")
    void updateUser_OnlyUsername_Success() {
        when(userAuthentication.getName()).thenReturn("test@example.com");
        List<GrantedAuthority> userAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(userAuthentication.getAuthorities()).thenReturn((Collection) userAuthorities);

        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                .username("newUsername")
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser("test@example.com", updateDTO, userAuthentication);

        assertEquals("newUsername", result.getUsername());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("updateUser - ignoring empty fields")
    void updateUser_EmptyValues_Ignored() {
        when(userAuthentication.getName()).thenReturn("test@example.com");
        List<GrantedAuthority> userAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(userAuthentication.getAuthorities()).thenReturn((Collection) userAuthorities);

        UserUpdateRequestDTO updateDTO = UserUpdateRequestDTO.builder()
                .username("")
                .password("")
                .build();

        String originalUsername = testUser.getUsername();
        String originalPassword = testUser.getPassword();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser("test@example.com", updateDTO, userAuthentication);

        assertEquals(originalUsername, result.getUsername());
        assertEquals(originalPassword, result.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("deleteUser - admin deletes user")
    void deleteUserByEmail_AsAdmin_Success() {
        List<GrantedAuthority> adminAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(adminAuthentication.getAuthorities()).thenReturn((Collection) adminAuthorities);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() -> userService.deleteUserByEmail("test@example.com", adminAuthentication));

        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    @DisplayName("deleteUser - default user cannot delete any profile")
    void deleteUserByEmail_AsUser_ThrowsAccessDeniedException() {
        List<GrantedAuthority> userAuthorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(userAuthentication.getAuthorities()).thenReturn((Collection) userAuthorities);

        CustomAccessDeniedException exception = assertThrows(
                CustomAccessDeniedException.class,
                () -> userService.deleteUserByEmail("test@example.com", userAuthentication)
        );

        assertEquals("Only admin can delete users.", exception.getMessage());
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    @DisplayName("deleteUser - user not found")
    void deleteUserByEmail_UserNotFound_ThrowsException() {
        List<GrantedAuthority> adminAuthorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(adminAuthentication.getAuthorities()).thenReturn((Collection) adminAuthorities);

        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUserByEmail("nonexistent@example.com", adminAuthentication)
        );

        verify(userRepository, never()).delete(any(User.class));
    }
}