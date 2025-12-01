package com.danialrekhman.userservice.controller;

import com.danialrekhman.userservice.dto.*;
import com.danialrekhman.userservice.mapper.UserMapper;
import com.danialrekhman.userservice.model.User;
import com.danialrekhman.userservice.security.JwtService;
import com.danialrekhman.userservice.service.TokenBlacklistService;
import com.danialrekhman.userservice.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDTO> signup(@Valid @RequestBody UserSignUpRequestDTO requestDTO) {
        User userToCreate = userMapper.toUser(requestDTO);
        User createdUser = userService.signUpUser(userToCreate);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toUserResponseDTO(createdUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO authRequestDTO) {
        User userCredentials = new User();
        userCredentials.setEmail(authRequestDTO.getEmail());
        userCredentials.setPassword(authRequestDTO.getPassword());
        String token = userService.verifyAndReturnToken(userCredentials);
        return ResponseEntity.ok(new AuthResponseDTO(token));
    }

//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(HttpServletRequest request) {
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//            tokenBlacklistService.blacklistToken(token);
//        }
//        return ResponseEntity.ok().build();
//    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            long ttl = jwtService.getRemainingValidity(token);
            if (ttl < 0) ttl = 0;
            tokenBlacklistService.blacklistToken(token, ttl);
        }
        return ResponseEntity.ok().build();
    }


    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam("token") String token) {
        userService.verifyUser(token);
        return ResponseEntity.ok("Email successfully verified!");
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(Authentication authentication) {
        List<User> users = userService.getAllUsers(authentication);
        return ResponseEntity.ok(userMapper.toUserResponseDTOs(users));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName(), authentication);
        return ResponseEntity.ok(userMapper.toUserResponseDTO(user));
    }

    @GetMapping("/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email, Authentication authentication) {
        User user = userService.getUserByEmail(email, authentication);
        return ResponseEntity.ok(userMapper.toUserResponseDTO(user));
    }

    @PutMapping("/{email}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable String email,
            @RequestBody UserUpdateRequestDTO updateDTO,
            Authentication authentication) {
        User updatedUser = userService.updateUser(email, updateDTO, authentication);
        return ResponseEntity.ok(userMapper.toUserResponseDTO(updatedUser));
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email, Authentication authentication) {
        userService.deleteUserByEmail(email, authentication);
        return ResponseEntity.noContent().build();
    }
}
