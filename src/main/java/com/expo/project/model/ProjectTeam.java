package com.expo.project.model;

import com.expo.project.model.Project;
import com.expo.teams.model.Team;
import com.expo.teams.model.TeamRole;
import lombok.*;

import jakarta.persistence.*;

@Entity
@Table(name = "project_teams")
@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class ProjectTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    private TeamRole role;
}
