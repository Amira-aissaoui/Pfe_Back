package com.expo.teams.controller;

import com.expo.project.model.Project;
import com.expo.project.model.ProjectTeam;
import com.expo.project.repo.ProjectRepository;
import com.expo.project.repo.ProjectTeamRepository;
import com.expo.security.model.User;
import com.expo.security.model.UserDTO;
import com.expo.security.repo.UserRepository;
import com.expo.teams.model.Team;
import com.expo.teams.model.TeamDTO;
import com.expo.teams.repo.TeamRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("api/teams")
public class TeamController {
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    // private final UserTeamRepository userTeamRepository;
    public TeamController(TeamRepository teamRepository, UserRepository userRepository, ProjectRepository projectRepository, ProjectTeamRepository projectTeamRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @GetMapping("/get_teams")
    public ResponseEntity<List<TeamDTO>> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        List<TeamDTO> teamDTOs = new ArrayList<>();

        for (Team team : teams) {
            TeamDTO teamDTO = new TeamDTO();
            teamDTO.setId(team.getId());
            teamDTO.setTeamName(team.getTeamName());

            List<User> users = team.getUsers();
            List<UserDTO> userDTOs = new ArrayList<>();

            for (User user : users) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(user.getId());
                userDTO.setFirstname(user.getFirstname());
                userDTO.setLastname(user.getLastname());
                userDTO.setEmail(user.getEmail());
                userDTO.setRole(user.getRole());
                userDTOs.add(userDTO);
            }

            teamDTO.setUsers(userDTOs);
            teamDTOs.add(teamDTO);
        }

        return ResponseEntity.ok(teamDTOs);
    }

    @PostMapping("add_team")
        public ResponseEntity<Long> addTeam(@RequestBody Team team) {
        Optional<Team> isteam=this.teamRepository.findByTeamName(team.getTeamName());
        if (isteam.isPresent()) {

            return ResponseEntity.ok(Long.valueOf(-1));
        }

        Team newTeam = teamRepository.save(team);
        return ResponseEntity.ok().body(newTeam.getId());
    }
/*


    @DeleteMapping("/delete_team")
    public ResponseEntity<Boolean> deleteTeam(@RequestParam(value = "teamname") String teamname) {
        Optional<Team> teamOptional = teamRepository.findByTeamName(teamname);

        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();

            // Before deleting the team, you might want to handle related data, like users in the team.
            // For example, if there is a one-to-many relationship between Team and User, you can delete users first.
            // userTeamRepository.deleteAllByTeam(team);

            teamRepository.delete(team);
            return ResponseEntity.ok(true); // Return true since the team was deleted successfully.
        } else {
            return ResponseEntity.ok(false); // Return false if the team with the given name doesn't exist.
        }
    }*/

    @DeleteMapping("/delete_team")
    public ResponseEntity<Boolean> deleteTeam(@RequestParam(value = "teamname") String teamname) {
        Optional<Team> teamOptional = teamRepository.findByTeamName(teamname);

        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();

            // Before deleting the team, let's handle the related data in "project_teams" table.
            // We need to remove the team from the list of teams associated with each project.
            List<Project> projects = projectRepository.findAll();

            for (Project project : projects) {
                List<Team> teams = project.getTeams();
                List<ProjectTeam> projectTeams=project.getProjectTeams();
                projectTeams.removeIf(t -> t.getTeam().getTeamName().equals(teamname)); // Remove the team from the project

                teams.removeIf(t -> t.getTeamName().equals(teamname)); // Remove the team from the project
            }

            // Before deleting the team, let's handle related data, like users.
            // For many-to-many relationships, we need to remove the team from users.
            for (User user : team.getUsers()) {
                user.getTeams().remove(team);
            }

            // Now we can safely delete the team
            teamRepository.delete(team);

            return ResponseEntity.ok(true); // Return true since the team was deleted successfully.
        } else {
            return ResponseEntity.ok(false); // Return false if the team with the given name doesn't exist.
        }
    }




    @PutMapping("/update_team")
    public ResponseEntity<Boolean> updateTeam(@RequestBody Team newteam, @RequestParam(value = "teamname") String teamname) {
        Optional<Team> isTeamExists=teamRepository.findByTeamName(newteam.getTeamName());
        if(isTeamExists.isPresent()){
            return ResponseEntity.ok(false);

        }

        Optional<Team> oldteamOptional = teamRepository.findByTeamName(teamname);
            Team oldteam = oldteamOptional.get();
            oldteam.setTeamName(newteam.getTeamName());

            teamRepository.save(oldteam);
            return ResponseEntity.ok(true);

    }
    @PostMapping("/{teamId}/users/{userId}")
    public ResponseEntity<Boolean> addUserToTeam(@PathVariable String teamId, @PathVariable String userId) {
        Optional<Team> teamOptional = teamRepository.findById(Long.parseLong(teamId));
        Optional<User> userOptional = userRepository.findById(Integer.parseInt( userId));

        if (teamOptional.isPresent() && userOptional.isPresent()) {
            Team team = teamOptional.get();
            User user = userOptional.get();

            team.addUser(user);
            teamRepository.save(team);

            return ResponseEntity.ok(true);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }



        @DeleteMapping("/{teamId}/users/{userId}")
    public ResponseEntity<Boolean> removeUserFromTeam(@PathVariable Long teamId, @PathVariable Long userId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        Optional<User> userOptional = userRepository.findById(Math.toIntExact(userId));

        if (teamOptional.isPresent() && userOptional.isPresent()) {
            Team team = teamOptional.get();
            User user = userOptional.get();

            team.removeUser(user);
            teamRepository.save(team);

            return ResponseEntity.ok(true);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }
        @GetMapping("/{teamId}/users")
    public ResponseEntity<List<UserDTO>> getTeamUsers(@PathVariable Long teamId) {
        Optional<Team> teamOptional = teamRepository.findById(teamId);

        if (teamOptional.isPresent()) {
            Team team = teamOptional.get();
            List<User> users = team.getUsers();

            // Convert User objects to UserDTO objects
            List<UserDTO> userDTOs = users.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(userDTOs);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstname(user.getFirstname());
        userDTO.setLastname(user.getLastname());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());

        return userDTO;
    }







}
