package com.danialrekhman.userservice.dto;

import com.danialrekhman.userservice.model.Role;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserResponseDTO {
    private String email;
    private String username;
    private Role role;
    private byte[] profileImage;
}
