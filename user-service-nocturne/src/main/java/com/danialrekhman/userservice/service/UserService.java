package com.danialrekhman.userservice.service;

import com.danialrekhman.userservice.dto.UserUpdateRequestDTO;
import com.danialrekhman.userservice.model.User;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface UserService {

    User signUpUser(User user);

    String verifyAndReturnToken(User user);

    User updateUser(String email, UserUpdateRequestDTO requestDTO, Authentication authentication);

    void deleteUserByEmail(String email, Authentication authentication);

    User getUserByEmail(String email, Authentication authentication);

    List<User> getAllUsers(Authentication authentication);
}
