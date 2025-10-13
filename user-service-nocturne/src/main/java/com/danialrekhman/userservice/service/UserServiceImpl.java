package com.danialrekhman.userservice.service;

import com.danialrekhman.commonevents.UserRegisteredEvent;
import com.danialrekhman.commonevents.VerificationEmailEvent;
import com.danialrekhman.userservice.dto.UserUpdateRequestDTO;
import com.danialrekhman.userservice.exception.*;
import com.danialrekhman.userservice.kafka.UserEventProducer;
import com.danialrekhman.userservice.model.Role;
import com.danialrekhman.userservice.model.User;
import com.danialrekhman.userservice.model.VerificationToken;
import com.danialrekhman.userservice.repository.UserRepository;
import com.danialrekhman.userservice.security.JwtService;
import com.danialrekhman.userservice.security.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserEventProducer userEventProducer;
    private final VerificationTokenService verificationTokenService;

    @Override
    public List<User> getAllUsers(Authentication authentication) {
        if (!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can see all users.");
        return userRepository.findAll();
    }

    @Override
    public User getUserByEmail(String email, Authentication authentication) {
       if (!isAdmin(authentication) && !email.equals(authentication.getName()))
            throw new CustomAccessDeniedException("You can only view your own profile.");
       if (!userRepository.existsByEmail(email))
           throw new UserNotFoundException("User with email '" + email + "' not found.");
        return userRepository.findByEmail(email);
    }

    @Override
    public User signUpUser(User user) {
        if (userRepository.existsByEmail(user.getEmail()))
            throw new DuplicateResourceException("User with email '" + user.getEmail() + "' already exists.");

        user.setRole(Role.ROLE_USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setVerified(false);
        User savedUser = userRepository.save(user);

        // Створюємо verification token
        VerificationToken vt = verificationTokenService.createTokenForUser(savedUser);

        // Відправляємо верифікаційний лист
        VerificationEmailEvent verificationEvent = VerificationEmailEvent.builder()
                .email(savedUser.getEmail())
                .username(savedUser.getUsername())
                .token(vt.getToken())
                .verificationUrl("http://localhost:3000/verify?token=" + vt.getToken())
                .build();
        userEventProducer.publishVerificationEmail(verificationEvent);

        return savedUser;
    }


    @Override
    public boolean verifyUser(String token) {
        VerificationToken vt = verificationTokenService.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (vt.getExpiryDate().isBefore(LocalDateTime.now())) {
            verificationTokenService.deleteToken(token);
            throw new RuntimeException("Verification token expired");
        }

        User user = vt.getUser();
        user.setVerified(true);
        userRepository.save(user);
        verificationTokenService.deleteToken(token);

        // Тільки тут відправляємо Welcome event
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .build();
        userEventProducer.publishUserRegistered(event);

        return true;
    }

    @Override
    public String verifyAndReturnToken(User userCredentials) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userCredentials.getEmail(), userCredentials.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
            User authenticatedUser = myUserDetails.getUser();
            return jwtService.generateToken(authenticatedUser.getEmail(), authenticatedUser.getRole().name());
        } catch (BadCredentialsException e) {
            throw new AuthenticationFailedException("Wrong email or password.");
        }
    }

    @Override
    public User updateUser(String email, UserUpdateRequestDTO userUpdateRequestDTO, Authentication authentication) {
        if (!userRepository.existsByEmail(email))
            throw new UserNotFoundException("User with email '" + email + "' not found.");
        User userToUpdate = userRepository.findByEmail(email);
        if (!isAdmin(authentication) && !userToUpdate.getEmail().equals(authentication.getName()))
            throw new CustomAccessDeniedException("You can only update your own profile.");
        if (userUpdateRequestDTO.getUsername() != null && !userUpdateRequestDTO.getUsername().isBlank())
            userToUpdate.setUsername(userUpdateRequestDTO.getUsername());
        if (userUpdateRequestDTO.getPassword() != null && !userUpdateRequestDTO.getPassword().isBlank())
            userToUpdate.setPassword(passwordEncoder.encode(userUpdateRequestDTO.getPassword()));
        return userRepository.save(userToUpdate);
    }

    @Override
    public void deleteUserByEmail(String email, Authentication authentication) {
        if (!isAdmin(authentication))
            throw new CustomAccessDeniedException("Only admin can delete users.");
        if (!userRepository.existsByEmail(email))
            throw new UserNotFoundException("User with email '" + email + "' not found.");
        userRepository.delete(userRepository.findByEmail(email));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }
}