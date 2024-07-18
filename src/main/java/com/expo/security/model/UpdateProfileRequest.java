package com.expo.security.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateProfileRequest {
    private AuthenticationRequest credentials;
    private User user;

}
