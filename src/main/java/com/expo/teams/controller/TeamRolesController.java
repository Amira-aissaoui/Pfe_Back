package com.expo.teams.controller;


import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.expo.project.model.Project;
import com.expo.project.model.ProjectTeam;
import com.expo.project.repo.ProjectRepository;
import com.expo.project.repo.ProjectTeamRepository;
import com.expo.teams.model.Team;
import com.expo.teams.model.TeamRole;
import com.expo.teams.model.TeamRoleDTO;
import com.expo.teams.repo.TeamRepository;
import com.expo.teams.repo.TeamRoleRepository;

@RestController
@RequestMapping("/api/project/{projectId}/team/{teamId}/roles")
@CrossOrigin(origins = "http://localhost:4200/")
public class TeamRolesController {
    private final TeamRoleRepository teamRoleRepository;
    private final ProjectTeamRepository projectTeamRepository;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;


    public TeamRolesController(TeamRoleRepository teamRoleRepository, ProjectTeamRepository projectTeamRepository, ProjectRepository projectRepository, TeamRepository teamRepository) {
        this.teamRoleRepository = teamRoleRepository;
        this.projectTeamRepository = projectTeamRepository;
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping
    public ResponseEntity<TeamRoleDTO> getTeamsRoleByProject(@PathVariable Long projectId,@PathVariable Long teamId) {
        TeamRoleDTO teamRoles = (this.teamRoleRepository.getTeamRoleByTeamIdAndProjectId(teamId,projectId));
        return ResponseEntity.ok(teamRoles);
    }

    @PutMapping("/{newRole}")
    public ResponseEntity<Boolean> changeTeamsRole(@PathVariable Long projectId, @PathVariable Long teamId, @PathVariable String newRole) {
        Optional<Project> optionalProject = projectRepository.findById(projectId);
        System.out.println("proj"+optionalProject.get());
        if (optionalProject.isPresent()) {
            Project project = optionalProject.get();

            // Find the team by its ID
            Optional<Team> optionalTeam = teamRepository.findById(teamId);
            if (optionalTeam.isPresent()) {
                Team team = optionalTeam.get();

             // Update the team role in the project
                for (ProjectTeam projectTeam : project.getProjectTeams()) {
                    if (projectTeam.getTeam().equals(team)) {
                        projectTeam.setRole(TeamRole.valueOf(newRole));
                        break;
                    }
                }

                projectRepository.save(project);
            } else {
                // Handle the case when the team is not found
                throw new RuntimeException("Team not found");
            }
        } else {
            // Handle the case when the project is not found
            throw new RuntimeException("Project not found");
        }
        return null;
    }
}
