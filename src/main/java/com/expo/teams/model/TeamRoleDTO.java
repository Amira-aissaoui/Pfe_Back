package com.expo.teams.model;

import com.expo.project.model.ProjectDTO;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class TeamRoleDTO {

    private Long projectId;
    private Long teamId;
    private String teamName;
    private TeamRole role;
    private ProjectDTO project;

    public TeamRoleDTO(Long projectId, Long teamId, TeamRole role) {
        this.projectId = projectId;
        this.teamId = teamId;
        this.role = role;
    }
}
