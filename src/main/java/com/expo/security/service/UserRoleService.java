package com.expo.security.service;

import com.expo.security.model.UserRole;
import com.expo.security.model.UserRoleDTO;
import com.expo.security.repo.UserRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public List<UserRoleDTO> getUserRolesByProjectAndUser(Long projectId, Long userId) {
        List<UserRole> userRoles = userRoleRepository.findByProjectIdAndUserId(projectId, userId);
        return userRoles.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserRoleDTO convertToDTO(UserRole userRole) {
        UserRoleDTO userRoleDTO = new UserRoleDTO();
        userRoleDTO.setRoleId(userRole.getId());
        userRoleDTO.setProjectId(userRole.getProject().getId());
        userRoleDTO.setUserId((userRole.getUser().getId()));
        userRoleDTO.setRole(userRole.getRole().name());


        return userRoleDTO;
    }

}


