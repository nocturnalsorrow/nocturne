package com.danialrekhman.userservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserUpdateRequestDTO {
    private String username;
    private String password;
}
