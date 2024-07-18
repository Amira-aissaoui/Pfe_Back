package com.expo.project.model;

import com.expo.security.model.User;
import com.expo.security.model.UserDTO;
import com.expo.security.model.UserRole;
import com.expo.security.model.UserRoleDTO;
import com.expo.teams.model.TeamDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class ProjectDTO {
    private Long id;
    private String projectName;
    private boolean monitoring;
    private boolean alerting;
    private String appType;
    private String ipAddresses;
    private String msnames;
    private String uid;
    private String deployment;
    private List<TeamDTO> teams;
    private List<UserDTO> users;

    private String Visibility;

}
