package com.danialrekhman.userservice.mapper;

import com.danialrekhman.userservice.dto.*;
import com.danialrekhman.userservice.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponseDTO toUserResponseDTO(User user) {
        if (user == null) return null;
        return UserResponseDTO.builder()
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .profileImage(user.getProfileImage())
                .build();
    }

    public List<UserResponseDTO> toUserResponseDTOs(List<User> users) {
        if (users == null) return null;
        return users.stream()
                .map(this::toUserResponseDTO)
                .collect(Collectors.toList());
    }

    public User toUser(UserSignUpRequestDTO dto) {
        if (dto == null) return null;
        return User.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .username(dto.getUsername())
                .build();
    }
}
