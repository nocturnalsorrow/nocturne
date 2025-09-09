package com.danialrekhman.userservice.dto;

import com.danialrekhman.userservice.model.Role;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponseDTO {
    String email;
    String username;
    Role role;
    byte[] profileImage;
}
