package com.expo.security.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expo.project.model.Project;
import com.expo.project.model.ProjectDTO;
import com.expo.project.repo.ProjectRepository;
import com.expo.project.repo.ProjectTeamRepository;
import com.expo.security.model.AuthenticationRequest;
import com.expo.security.model.Token;
import com.expo.security.model.UpdateProfileRequest;
import com.expo.security.model.User;
import com.expo.security.model.UserDTO;
import com.expo.security.model.UserProjectRole;
import com.expo.security.model.UserProjectsDTO;
import com.expo.security.model.UserRole;
import com.expo.security.repo.TokenRepository;
import com.expo.security.repo.UserRepository;
import com.expo.security.repo.UserRoleRepository;
import com.expo.security.service.UserService;
import com.expo.teams.model.Team;
import com.expo.teams.model.TeamDTO;
import com.expo.teams.model.TeamRole;
import com.expo.teams.model.TeamRoleDTO;
import com.expo.teams.repo.TeamRepository;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "http://localhost:4200/")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final ProjectTeamRepository projectTeamRepository;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, ProjectRepository projectRepository, UserRoleRepository userRoleRepository, TeamRepository teamRepository, ProjectTeamRepository projectTeamRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.teamRepository = teamRepository;
        this.projectTeamRepository = projectTeamRepository;
    }

    // yetbadel l esm ; finduserproject
    @GetMapping("/user/user_info/{email}")
    public ResponseEntity<UserProjectsDTO> getUserInfo(@PathVariable String email) {
        Optional<User> user = this.userRepository.findByEmail(email);
        System.out.println("HEREEE");
        if (user.isPresent()) {
            Long userId = Long.valueOf(user.get().getId());
            List<Project> projects = projectRepository.findByUserId(userId);
            List<ProjectDTO> projectsDTO = new ArrayList<>();
            Map<Long, String> userProjectRoles = new HashMap<>();
            UserProjectsDTO userProjectsDTO = new UserProjectsDTO();
            userProjectsDTO.setUser(userService.convertUserToDTO(user.get()));
            for (Project project : projects) {
                System.out.println("HEREEE2");

                projectsDTO.add(userService.convertProjectToDTO(project));
                userProjectRoles.put(project.getId(), userService.getUserRole((long) userId,project.getId()));
            }
            userProjectsDTO.setProjects(projectsDTO);
            userProjectsDTO.setUserRole(userProjectRoles);

            return ResponseEntity.ok(userProjectsDTO);
        }

        return ResponseEntity.notFound().build();
    }



    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> userDetailsArray = userService.getAllUsers();
        return ResponseEntity.ok(userDetailsArray);
    }


    @DeleteMapping("/delete_users")
    public ResponseEntity<Boolean> deleteUser(@RequestParam String email) {
        Optional<User> user = this.userRepository.findByEmail(email);
        if (user.isPresent()) {
            List<Token> tokens=tokenRepository.findAllValidTokenByUser(user.get().getId());
            for (Token t:tokens
            ) {
                tokenRepository.delete(t);
            }
            List<UserRole> userRoles = user.get().getUserRoles();
            for (UserRole userRole : userRoles) {
                userRole.setUser(null); // Remove the association with the user
            }
            userRoles.clear(); // Clear the list of user roles

            this.userRepository.delete(user.get());

            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }

    @PutMapping("/update_user")
    public ResponseEntity<Boolean> updateUser(@RequestParam String email, @RequestBody User updatedUser) {

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Optional<User> userOptional2 = userRepository.findByEmail(updatedUser.getEmail());
            user.setFirstname(updatedUser.getFirstname());
            user.setLastname(updatedUser.getLastname());
            if(!userOptional2.isPresent()){
                user.setEmail(updatedUser.getEmail());

            }
            user.setRole(updatedUser.getRole());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            userRepository.save(user);
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/update_profile")
    public ResponseEntity<Boolean> updateProfile(@RequestBody UpdateProfileRequest request) {
        AuthenticationRequest credentials = request.getCredentials();
        User updatedUser = request.getUser();
        System.out.println(updatedUser.getPassword());
        if (credentials == null) {
            return ResponseEntity.badRequest().build();
        }
        System.out.println("User Firstname: " + updatedUser);

        System.out.println("User Firstname: " + updatedUser.getFirstname());
        System.out.println("User Password: " + updatedUser.getPassword());
        System.out.println("User Email: " + updatedUser.getEmail());
        System.out.println("User Lastname: " + updatedUser.getLastname());
        System.out.println("Email: " + credentials.getEmail());

        // Retrieve the user from the repository using the provided email
        Optional<User> userOptional = userRepository.findByEmail(credentials.getEmail());
        System.out.println("userOptional: " + userOptional.get().getPassword());

        if (userOptional.isPresent() && passwordEncoder.matches(credentials.getPassword(), userOptional.get().getPassword())) {
            // Update the user properties
            User user = userOptional.get();
            user.setFirstname(updatedUser.getFirstname());
            user.setLastname(updatedUser.getLastname());
            user.setEmail(updatedUser.getEmail());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }
            // Save the updated user
            userRepository.save(user);
            return ResponseEntity.ok(true);
        }
        // If the user or password does not match, return a not found response
        return ResponseEntity.notFound().build();
    }


    /*   @GetMapping("/get_teams_users")
    public ResponseEntity<List<User>> getUsersByTeamName(@RequestParam String teamName) {
        List<User> users = userRepository.findByTeamsTeamNameIgnoreCase(teamName);
        return ResponseEntity.ok(users);
    }*/
 @PostMapping("/users/{userId}/projects/{projectId}/{role}")
 public ResponseEntity<Boolean> addProjectToUser(@PathVariable Integer userId, @PathVariable Long projectId,@PathVariable String role) {
     Optional<User> userOptional = userRepository.findById(userId);
     Optional<Project> projectOptional = projectRepository.findById(projectId);

     if (userOptional.isPresent() && projectOptional.isPresent()) {
         User user = userOptional.get();
         Project project = projectOptional.get();

         // Check if the user already has the project assigned
         boolean isProjectAssigned = user.getProjects().stream()
                 .anyMatch(existingProject -> existingProject.getId().equals(projectId));

         if (isProjectAssigned) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
         }

         // Add the project to the user's list of projects
         user.getProjects().add(project);
         // Add the user to the project's list of users
         project.getUsers().add(user);

         // Assign a role to the user in the project (assuming 'role' is the desired role)
         this.assignRoleToProject(project, role, user);

         userRepository.save(user);
         projectRepository.save(project);

         return ResponseEntity.ok(true);
     }

     return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
 }


    public void assignRoleToProject(Project project, String role, User user) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setProject(project);
        userRole.setRole(UserProjectRole.valueOf(role));

        user.getUserRoles().add(userRole);
        project.getUserRoles().add(userRole);
    }

    @PutMapping("/users/{userId}/projects/{projectId}/{role}")
    public ResponseEntity<Boolean> updateUserRoleInProject(@PathVariable Integer userId, @PathVariable Long projectId, @PathVariable UserProjectRole role) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (userOptional.isPresent() && projectOptional.isPresent()) {
            User user = userOptional.get();
            Project project = projectOptional.get();

            // Find the existing user role in the project
            Optional<UserRole> userRoleOptional = user.getUserRoles().stream()
                    .filter(userRole -> userRole.getProject().equals(project))
                    .findFirst();

            if (userRoleOptional.isPresent()) {
                UserRole userRole = userRoleOptional.get();
                userRole.setRole(role);
                userRepository.save(user);

                return ResponseEntity.ok(true);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    @DeleteMapping("/users/{userId}/projects/{projectId}")
    public ResponseEntity<Boolean> removeUserFromProject(@PathVariable Integer userId, @PathVariable Long projectId) {
        System.out.println("userOptional"+userId);
        System.out.println("userOptional"+projectId);

        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (userOptional.isPresent() && projectOptional.isPresent()) {
            User user = userOptional.get();
            Project project = projectOptional.get();

            // Check if the user is assigned to the project
          /*  boolean isUserAssigned = user.getProjects().stream()
                    .anyMatch(existingProject -> existingProject.getId().equals(projectId));*/
            boolean isUserAssigned=false;
            for(Project projectsearch:user.getProjects()){
                    if(projectsearch.getId().equals(projectId)){
                        isUserAssigned=true;
                    }

            }

            if (!isUserAssigned) {
                System.out.println("lenaaaaa");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
            }

            // Remove the project from the user's list of projects
            user.getProjects().remove(project);
            // Remove the user from the project's list of users
            project.getUsers().remove(user);

            // Remove the user role in the project
            removeUserRole(user, project);

            userRepository.save(user);
            projectRepository.save(project);

            return ResponseEntity.ok(true);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    private void removeUserRole(User user, Project project) {
        user.getUserRoles().removeIf(userRole -> userRole.getProject().equals(project));
    }

   /* @GetMapping("/{userId}/teams")
    public ResponseEntity<List<Team>> getUserTeams(@PathVariable Long userId) {
        List<Team> teams = teamRepository.findTeamsByUserId(userId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{userId}/teams/projects")
    public ResponseEntity<List<ProjectDTO>> getUserTeamsWithProjects(@PathVariable Long userId) {
        List<Team> teams = teamRepository.findTeamsByUserId(userId);

        List<ProjectDTO> projects = teams.stream()
                .flatMap(team -> team.getProjects().stream())
                .map(ProjectDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(projects);
    }*/
   @GetMapping("/{userId}/teams")
   public ResponseEntity<List<TeamDTO>> getUserTeams(@PathVariable Integer userId) {
       List<Team> teams = teamRepository.findTeamsByUserId((long) userId);
       List<TeamDTO> teamDTOs =new ArrayList<>();
       for(Team team:teams){
           for(User user:team.getUsers()){
               if(user.getId().equals(userId)){
                //   teamDTOs.add(this.convertToTeamDTO(team));

               }
           }
       }
       return ResponseEntity.ok(teamDTOs);
   }

   /* @GetMapping("/{userId}/teams/projects")
    public ResponseEntity<List<ProjectDTO>> getUserTeamsWithProjects(@PathVariable Integer userId) {
        List<Team> teams = teamRepository.findTeamsByUserId((long)userId);
        System.out.println(teams.get(0).getTeamName());
        System.out.println(teams.get(0).getProjects().get(0).getProjectName());

      /*  List<ProjectDTO> projects = teams.stream()
                .flatMap(team -> team.getProjects().stream())
                .map(ProjectDTO::new)
                .collect(Collectors.toList());
        List<ProjectDTO> projects=new ArrayList<>();
        for(int i=0;i<teams.size();i++){
            for(Project project:teams.get(i).getProjects()){
                projects.add(this.convertProjectToDTO(project));
            }
        }
        return ResponseEntity.ok(projects);
    }
*/
  /* @GetMapping("/{userId}/teams/projects")
   public ResponseEntity<List<ProjectDTO>> getUserTeamsWithProjects(@PathVariable Integer userId) {
       List<Team> teams = teamRepository.findTeamsByUserId((long) userId);
       List<ProjectDTO> projects = new ArrayList<>();

       for (Team team : teams) {
           List<Project> teamProjects = team.getProjects();
           for (Project project : teamProjects) {
               projects.add(convertProjectToDTO(project));
           }
       }

       return ResponseEntity.ok(projects);
   }

    private TeamDTO convertToTeamDTO(Team team) {
        TeamDTO teamDTO=new TeamDTO();
        teamDTO.setId(team.getId());
        teamDTO.setTeamName(team.getTeamName());
        List<UserDTO> usersDTO=new ArrayList<>();
        List<ProjectDTO> projectsDTO=new ArrayList<>();

        for( User user:team.getUsers()){
            usersDTO.add(this.convertUserToDTO(user));
        }
        for( Project project:team.getProjects()){
            projectsDTO.add(this.convertProjectToDTO(project));
        }
        teamDTO.setUsers(usersDTO);
        teamDTO.setProjects(projectsDTO);
        return teamDTO;
    }*/

    @GetMapping("/{userId}/teams/projects")
    public ResponseEntity<List<TeamRoleDTO>> getUserTeamsWithProjects(@PathVariable Integer userId) {
        List<Team> teams = teamRepository.findTeamsByUserId((long) userId);
        List<TeamRoleDTO> teamRoleDTOS = new ArrayList<>();

        for (Team team : teams) {
            List<Project> teamProjects = team.getProjects();
            for (Project project : teamProjects) {
                TeamRole teamRole = projectTeamRepository.findRoleByProjectAndTeam(project.getId(), team.getId());
                ProjectDTO projectDTO = userService.convertProjectToDTO(project);
                TeamRoleDTO teamRoleDTO = new TeamRoleDTO(projectDTO.getId(), team.getId(), team.getTeamName(),teamRole, projectDTO);
                teamRoleDTOS.add(teamRoleDTO);
            }
        }

        return ResponseEntity.ok(teamRoleDTOS);
    }



}
