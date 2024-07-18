package com.expo.security.service;

import com.expo.project.model.Project;
import com.expo.project.model.ProjectDTO;
import com.expo.project.repo.ProjectRepository;
import com.expo.project.repo.ProjectTeamRepository;
import com.expo.security.model.User;
import com.expo.security.model.UserDTO;
import com.expo.security.model.UserRole;
import com.expo.security.repo.TokenRepository;
import com.expo.security.repo.UserRepository;
import com.expo.security.repo.UserRoleRepository;
import com.expo.teams.model.Team;
import com.expo.teams.model.TeamDTO;
import com.expo.teams.repo.TeamRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private  final UserRoleRepository userRoleRepository;
    private final TeamRepository teamRepository;
    private final ProjectTeamRepository projectTeamRepository;


    @Autowired
    public UserService(UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, ProjectRepository projectRepository, UserRoleRepository userRoleRepository, TeamRepository teamRepository, ProjectTeamRepository projectTeamRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.projectRepository = projectRepository;
        this.userRoleRepository = userRoleRepository;
        this.teamRepository = teamRepository;
        this.projectTeamRepository = projectTeamRepository;
    }
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        ObjectMapper objectMapper = new ObjectMapper();
        List<UserDTO> userDetailsArray = new ArrayList<>();

        for (User user : users) {
            UserDTO userDetails=new UserDTO(user.getId(),user.getFirstname(),user.getLastname(),user.getEmail(),user.getRole());

            userDetailsArray.add(userDetails);
        }

        return userDetailsArray;
    }

    public ProjectDTO convertProjectToDTO(Project project) {
        List <UserDTO> usersDTO=new ArrayList<>();
        List<TeamDTO> teamsDTO=new ArrayList<>();
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setId(project.getId());
        projectDTO.setProjectName(project.getProjectName());
        projectDTO.setMonitoring(project.isMonitoring());
        projectDTO.setAlerting(project.isAlerting());
        projectDTO.setAppType(project.getAppType());
        projectDTO.setIpAddresses(project.getIpAddresses());
        projectDTO.setMsnames(project.getMsnames());
        projectDTO.setUid(project.getUid());
        projectDTO.setDeployment(project.getDeployment());
        for(User user: project.getUsers()){
            usersDTO.add(this.convertUserToDTO(user));
        }
        for(Team team: project.getTeams()){
            teamsDTO.add(this.convertTeamToDTO(team));
        }
        projectDTO.setVisibility(project.getVisibility());
        projectDTO.setUsers(usersDTO);
        return projectDTO;
    }
    private TeamDTO convertTeamToDTO(Team team) {
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setId(team.getId());
        teamDTO.setTeamName(team.getTeamName());
        return teamDTO;
    }
    public UserDTO convertUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(Math.toIntExact(user.getId()));
        userDTO.setFirstname(user.getFirstname());
        userDTO.setLastname(user.getLastname());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());

        return userDTO;
    }


    public String getUserRole(Long userId, Long projectID) {
        List<UserRole> userRoles = userRoleRepository.findByProjectIdAndUserId(projectID, userId);
        if (!userRoles.isEmpty()) {
            UserRole userRole = userRoles.get(0);
            return userRole.getRole().toString();
        }
        return null;
    }
    private ProjectDTO convertUserProjectToDTO(Project project) {
        ProjectDTO projectDTO = new ProjectDTO();
        // Set properties of the projectDTO
        projectDTO.setId(project.getId());
        projectDTO.setProjectName(project.getProjectName());
        projectDTO.setAlerting(project.isAlerting());
        projectDTO.setMonitoring(project.isMonitoring());
        projectDTO.setAppType(project.getAppType());
        projectDTO.setIpAddresses(project.getIpAddresses());
        projectDTO.setMsnames(project.getMsnames());
        projectDTO.setUid(project.getUid());
        projectDTO.setDeployment(project.getDeployment());
        projectDTO.setTeams(convertTeamsToDTO(project.getTeams()));
        projectDTO.setUsers(convertUsersToDTO(project.getUsers()));
        projectDTO.setVisibility(project.getVisibility());

        return projectDTO;
    }

    private List<TeamDTO> convertTeamsToDTO(List<Team> teams) {
        List<TeamDTO> teamDTOs = new ArrayList<>();
        for (Team team : teams) {
            TeamDTO teamDTO = new TeamDTO();
            // Set properties of the teamDTO
            teamDTO.setId(team.getId());
            teamDTO.setTeamName(team.getTeamName());
            teamDTOs.add(teamDTO);
        }
        return teamDTOs;
    }

    private List<UserDTO> convertUsersToDTO(List<User> users) {
        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users) {
            UserDTO userDTO = new UserDTO();
            // Set properties of the userDTO
            userDTO.setId(user.getId());
            userDTO.setFirstname(user.getFirstname());
            userDTO.setLastname(user.getLastname());
            userDTO.setEmail(user.getEmail());
            userDTO.setRole(user.getRole());
            userDTOs.add(userDTO);
        }
        return userDTOs;
    }


}
