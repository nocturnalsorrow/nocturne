package com.danialrekhman.userservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserSignUpRequestDTO {
    private String email;
    private String password;
    private String username;
}
