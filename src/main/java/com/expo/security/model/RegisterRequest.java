package com.expo.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    public static Object builder() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;

    /*public String getEmail() { // Getter method to access email
        return email;
    }

    public String getFirstname() { // Getter method to access email
        return firstname;
    }

*/

}

