package com.expo.teams.model;

import com.expo.project.model.ProjectDTO;
import com.expo.security.model.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter

@AllArgsConstructor
@NoArgsConstructor
public class TeamDTO {
    private Long id;
    private String teamName;
    private List<UserDTO> users;
    private List<ProjectDTO> projects;


}
