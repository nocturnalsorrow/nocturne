package com.danialrekhman.userservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthRequestDTO {
    private String email;
    private String password;
}
