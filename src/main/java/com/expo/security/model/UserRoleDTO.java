package com.expo.security.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleDTO {
    private Long roleId;
    private Long projectId;
    private Integer userId;
    private String role;


}
