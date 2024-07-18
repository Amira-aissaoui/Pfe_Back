package com.expo.security.model;

import com.expo.project.model.ProjectDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor

@Getter
@Setter
@NoArgsConstructor
public class UserProjectsDTO {
    private UserDTO user;
    private List<ProjectDTO> projects;

    private Map<Long,String> userRole;

}
