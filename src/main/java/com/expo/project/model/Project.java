package com.expo.project.model;

import com.expo.security.model.User;
import com.expo.security.model.UserRole;
import com.expo.teams.model.Team;
import com.expo.teams.model.TeamRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "projects")
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name cannot be blank")
    @Column(name = "project_name")
    private String projectName;

    @NotNull(message = "Monitoring flag cannot be null")
    @Column(name = "monitoring")
    private boolean monitoring;

    @NotNull(message = "Alerting flag cannot be null")
    @Column(name = "alerting")
    private boolean alerting;

    @NotBlank(message = "App type cannot be blank")
    @Column(name = "app_type")
    private String appType;

    @NotBlank(message = "IP address cannot be blank")
    @Column(name = "ip_addresses")
    private String ipAddresses;

    @Column(name = "ms_names")
    private String msnames;

    @Column(name = "dashboard_uid")
    private String uid;

    @Column(name = "deployment")
    private String deployment;
    @Column(name="visibility")
    private String visibility;
    /*
    @Column(name="owner")
    private String owner;*/

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "project_team_mapping",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private List<Team> teams;



    @ManyToMany(mappedBy = "projects")
    private List<User> users;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<UserRole> userRoles;

    @ElementCollection
    @CollectionTable(name = "project_teams", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "team_role")
    @Enumerated(EnumType.STRING)
    private List<TeamRole> teamRoles;
// A VOIIIIIIIIIIIIIIR
    @OneToMany(mappedBy = "project",fetch = FetchType.EAGER ,cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTeam> projectTeams;



    public List<ProjectTeam> getProjectTeams() {
        return projectTeams;
    }

    public void setProjectTeams(List<ProjectTeam> projectTeams) {
        this.projectTeams = projectTeams;
    }
    public Project() {
    }
}
