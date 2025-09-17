package com.danialrekhman.commonevents;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationEmailEvent {
    private String email;
    private String username;
    private String token;
    // frontend/baseUrl field
    private String verificationUrl; // optional
}

