package com.danialrekhman.userservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
}
