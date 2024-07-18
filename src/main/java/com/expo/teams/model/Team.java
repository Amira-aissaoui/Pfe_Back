package com.expo.teams.model;

import com.expo.project.model.Project;
import com.expo.security.model.User;
import lombok.*;

import com.expo.project.model.Project;
import com.expo.security.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "teams")
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String teamName;

    @ManyToMany(mappedBy = "teams",fetch = FetchType.EAGER,cascade = CascadeType.ALL)

    private List<Project> projects;
    @ManyToMany(mappedBy = "teams",fetch = FetchType.EAGER)
    private List<User> users;



    public void addUser(User user) {
        users.add(user);
        user.getTeams().add(this);
    }

    public void removeUser(User user) {
        users.remove(user);
        user.getTeams().remove(this);
    }

}
