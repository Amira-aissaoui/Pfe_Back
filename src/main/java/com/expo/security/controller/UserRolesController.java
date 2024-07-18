package com.expo.security.controller;


import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expo.security.model.UserRoleDTO;
import com.expo.security.service.UserRoleService;

@RestController
@RequestMapping("/api/projects/{projectId}/users/{userId}/roles")
@CrossOrigin(origins = "http://localhost:4200/")
public class UserRolesController {

    private final UserRoleService userRoleService;

    public UserRolesController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @GetMapping
    public List<UserRoleDTO> getUserRolesByProjectAndUser(@PathVariable Long projectId, @PathVariable Long userId) {
        return userRoleService.getUserRolesByProjectAndUser(projectId, userId);
    }
}
