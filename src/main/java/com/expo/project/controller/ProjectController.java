package com.expo.project.controller;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.expo.grafana.controller.DashboardController;
import com.expo.grafana.service.GrafanaClient;
import com.expo.project.model.Project;
import com.expo.project.model.ProjectDTO;
import com.expo.project.model.ProjectTeam;
import com.expo.project.repo.ProjectRepository;
import com.expo.project.service.ProjectService;
import com.expo.project.service.ProjectServiceImpl;
import com.expo.prometheus.controller.PrometheusQueryController;
import com.expo.prometheus.service.AlertFileGenerator;
import com.expo.prometheus.service.PrometheusAlertService;
import com.expo.prometheus.service.PrometheusConfigFileGenerator;
import com.expo.prometheus.service.RuleFileGenerator;
import com.expo.security.model.User;
import com.expo.security.model.UserDTO;
import com.expo.security.model.UserRole;
import com.expo.security.repo.UserRoleRepository;
import com.expo.teams.model.Team;
import com.expo.teams.model.TeamDTO;
import com.expo.teams.model.TeamRole;
import com.expo.teams.repo.TeamRepository;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/project")
@CrossOrigin(origins = "http://localhost:4200/")
public class ProjectController {

    @Autowired
    private ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final UserRoleRepository userRoleRepository;
    private final TeamRepository teamRepository;
    private final ProjectServiceImpl projectServiceimpl;
    private final AlertFileGenerator alertFileGenerator;
    private final RuleFileGenerator ruleFileGenerator;
    private final GrafanaClient grafanaClient;
    private final PrometheusAlertService prometheusService;
    private static final String RULES_FILE_NAME = "alert.rules.yml";
    private static final String RESOURCES_DIRECTORY = "src/main/resources/";
    private final PrometheusConfigFileGenerator prometheusConfigFileGenerator;
    private final PrometheusQueryController prometheusQueryController;
    private final DashboardController dashboardController;
    @Value("${global.metric.url}")
    private String global_url;


    private String theLocalRulesFile=RESOURCES_DIRECTORY+RULES_FILE_NAME;

    public ProjectController(ProjectRepository projectRepository, UserRoleRepository userRoleRepository, TeamRepository teamRepository, ProjectServiceImpl projectServiceimpl, AlertFileGenerator alertFileGenerator, RuleFileGenerator ruleFileGenerator, GrafanaClient grafanaClient, PrometheusAlertService prometheusService, PrometheusConfigFileGenerator prometheusConfigFileGenerator, PrometheusQueryController prometheusQueryController, DashboardController dashboardController) {
        this.projectRepository = projectRepository;
        this.userRoleRepository = userRoleRepository;
        this.teamRepository = teamRepository;
        this.projectServiceimpl = projectServiceimpl;
        this.alertFileGenerator = alertFileGenerator;
        this.ruleFileGenerator = ruleFileGenerator;
        this.grafanaClient = grafanaClient;
        this.prometheusService = prometheusService;
        this.prometheusConfigFileGenerator = prometheusConfigFileGenerator;
        this.prometheusQueryController = prometheusQueryController;
        this.dashboardController = dashboardController;
    }

/*
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok("Project deleted successfully");
    }*/
    @PutMapping("/{projectId}")
    @Transactional
    public ResponseEntity<String> updateProjectField(@PathVariable Long projectId,
                                                     @RequestParam(value = "field") String fieldName,
                                                     @RequestParam(value="newvalue")String newValue,
                                                     @RequestParam(value = "oldvalue") String oldvalue) throws Exception {
        boolean updateSuccess = this.projectServiceimpl.changeprojectinfo(projectId, fieldName,oldvalue ,newValue);
        Optional<Project> project = projectRepository.findById(projectId);
        int tag=this.projectServiceimpl.getInstanceTagByInstance(oldvalue,projectId);
        System.out.println("tag"+tag);
        dashboardController.modifyallpanelsinstances(project.get().getProjectName(), String.valueOf(tag),newValue);

        if (updateSuccess) {
            if(fieldName.equals(("ipAddresses"))){
                boolean ismodified=this.ruleFileGenerator.modifyInstanceRules(oldvalue, newValue);
                if(ismodified){
                    boolean isalertmodified=this.alertFileGenerator.modifyInstanceInAlertFile(oldvalue, newValue);
                    if(isalertmodified){
                        if(tag!=-1){
                            dashboardController.modifyallpanelsinstances(project.get().getProjectName(), String.valueOf(tag),newValue);

                        }


                        return ResponseEntity.ok("Project field '" + fieldName + "' updated successfully.");

                    }

                }

            }
            return ResponseEntity.ok("Project field '" + fieldName + "' updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project with ID " + projectId + " not found.");
        }
    }

    @PostMapping(value = "/save-doproject")
    public ResponseEntity<?> saveDoProject(@RequestBody Project project) {
        try {
            System.out.println(project.getProjectName());
            System.out.println(project.getIpAddresses());
            System.out.println(project.getAppType());
            System.out.println(project.getClass());
            System.out.println(project);

            String[] ipAddressAndPortArray = project.getIpAddresses().split(",");

            for (String ipAddressAndPort : ipAddressAndPortArray) {
                String[] ipAndPort = ipAddressAndPort.split(":");

                if (ipAndPort.length != 2) {
                    continue;
                }

                String ip = ipAndPort[0];
                String port = ipAndPort[1];

                prometheusConfigFileGenerator.addAppToFile(project.getProjectName(), global_url, ip, port);
            }
            Thread.sleep(6000);
            String deployment = "";
                String ip = ipAddressAndPortArray[0].split(":")[0];
                String port = ipAddressAndPortArray[0].split(":")[1];
                if (this.prometheusQueryController.checkDeployment(ip, port).get("Deployment").asText() != "" ||
                        this.prometheusQueryController.checkDeployment(ip, port).get("Deployment").asText() != null) {
                    deployment = this.prometheusQueryController.checkDeployment(ip, port).get("Deployment").asText();
                }
                project.setDeployment(deployment);


            projectService.saveProject(project);
            Long id = projectRepository.findByProjectName(project.getProjectName()).getId();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(id);

        } catch (Exception e) {
            String errorMsg = "Error saving project: " + e.getMessage() + "\n" + e.toString();
            return new ResponseEntity<>(errorMsg, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/getproject")
    public ResponseEntity<ProjectDTO> getProjectById(@RequestParam(value = "id") String id) {
        Project project = projectService.getProjectById(Integer.parseInt(id));
        if (project != null) {
            ProjectDTO projectDTO = convertToDTO(project); // Convert Project to ProjectDTO
            return ResponseEntity.ok(projectDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/public-projects")
    public ResponseEntity<List<ProjectDTO>> getPublicProjects() {
        List<Project> publicProjects = projectRepository.findAllByVisibility("Public");
        List<ProjectDTO> projectDTOs = publicProjects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectDTOs);
    }

    @GetMapping("/getprojectbyname")
    public ResponseEntity<ProjectDTO> getProjectByName(@RequestParam(value = "projectname") String projectName) {
        System.out.println("projectName"+projectName);
        Project project = projectService.getProjectByName(projectName);

        if (project != null) {
            ProjectDTO projectDTO = convertToDTO(project);
            return ResponseEntity.ok(projectDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/get-all-projects")
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        List<ProjectDTO> projectDTOs = new ArrayList<>();

        for (Project project : projects) {
            ProjectDTO projectDTO = convertToDTO(project);
            projectDTOs.add(projectDTO);
        }

        if (!projectDTOs.isEmpty()) {
            return ResponseEntity.ok(projectDTOs);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/projects/{id}/ip-addresses")
    public ResponseEntity<List<Map<String, String>>> getIpAddressesByProjectId(@PathVariable Long id) {
        List<Map<String, String>> ipAddresses = projectService.getIpAddressesByProjectId(Long.valueOf(id));
        if (ipAddresses != null && !ipAddresses.isEmpty()) {
            return ResponseEntity.ok(ipAddresses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    private ProjectDTO convertToDTO(Project project) {
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
        projectDTO.setVisibility(project.getVisibility());

        List<UserDTO> userDTOs = project.getUsers().stream()
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
        projectDTO.setUsers(userDTOs);
        List<TeamDTO> teamDTOs = project.getTeams().stream()
                .map(this::convertTeamToDTO)
                .collect(Collectors.toList());
        projectDTO.setTeams(teamDTOs);


        return projectDTO;
    }
    private UserDTO convertUserToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstname(user.getFirstname());
        userDTO.setLastname(user.getLastname());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());

        return userDTO;
    }
    private TeamDTO convertTeamToDTO(Team team) {
        TeamDTO teamDTO = new TeamDTO();
        teamDTO.setId(team.getId());
        teamDTO.setTeamName(team.getTeamName());
        return teamDTO;
    }

    @PutMapping("/projects/{projectId}")
    public ResponseEntity<String> updateProject(@PathVariable Long projectId, @RequestBody ProjectDTO updatedProject) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isPresent()) {
            Project existingProject = projectOptional.get();

            if (Objects.nonNull(updatedProject.getProjectName())) {
                existingProject.setProjectName(updatedProject.getProjectName());
            }
            if (Objects.nonNull(updatedProject.getAppType())) {
                existingProject.setAppType(updatedProject.getAppType());
            }
            if (Objects.nonNull(updatedProject.getIpAddresses())) {
                existingProject.setIpAddresses(updatedProject.getIpAddresses());
            }
            if (Objects.nonNull(updatedProject.getMsnames())) {
                existingProject.setMsnames(updatedProject.getMsnames());
            }
            if (Objects.nonNull(updatedProject.isMonitoring())) {
                existingProject.setMonitoring(updatedProject.isMonitoring());
            }
            if (Objects.nonNull(updatedProject.isAlerting())) {
                existingProject.setAlerting(updatedProject.isAlerting());
            }
            if (Objects.nonNull(updatedProject.getVisibility())) {
                existingProject.setVisibility(updatedProject.getVisibility());
            }



            projectRepository.save(existingProject);
            return ResponseEntity.ok("Project updated successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found.");
    }
    //badalt lena awl boucle taa users
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);

        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();

            // Remove user roles associated with the project
            List<UserRole> userRoles = project.getUserRoles();
            for (UserRole userRole : userRoles) {
                userRoleRepository.delete(userRole);
            }

            List<User> users = project.getUsers();
            for (User user : users) {
                user.getProjects().remove(project);

                List<Team> teams = user.getTeams();
                for (Team team : teams) {
                    team.getProjects().remove(project);
                }

                List<UserRole> userRoles2 = user.getUserRoles();
                for (UserRole userRole : userRoles2) {
                    if (userRole.getProject().equals(project)) {
                        userRoleRepository.delete(userRole);
                    }
                }
            }

            List<Team> teams = project.getTeams();
            for (Team team : teams) {
                team.getProjects().remove(project);
            }
            this.grafanaClient.deleteDashboard(project.getProjectName());


            projectRepository.delete(project);

            return ResponseEntity.ok("Project deleted successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project not found.");
    }


    // fiha mochkla , moch ifassakh m table projectTeam
    @DeleteMapping("/projects/{projectId}/teams/{teamId}")
    public ResponseEntity<String> removeTeamFromProject(@PathVariable Long projectId, @PathVariable Long teamId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        Optional<Team> teamOptional = teamRepository.findById(teamId);

        if (projectOptional.isPresent() && teamOptional.isPresent()) {
            Project project = projectOptional.get();
            Team team = teamOptional.get();

            // Check if the team is assigned to the project
            boolean isTeamAssigned = project.getTeams().stream()
                    .anyMatch(existingTeam -> existingTeam.getId().equals(teamId));

            if (!isTeamAssigned) {
                return ResponseEntity.badRequest().body("Team is not assigned to the project.");
            }

            // Remove the team from the project's list of teams
            project.getTeams().removeIf(existingTeam -> existingTeam.getId().equals(teamId));

            // Remove the project from the team's list of projects
            team.getProjects().remove(project);

            projectRepository.save(project);
            teamRepository.save(team);

            return ResponseEntity.ok("Team removed from the project successfully.");
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Project or team not found.");
    }

    @PostMapping("/projects/{projectId}/teams/{teamId}/{role}")
    public ResponseEntity<Boolean> addTeamToProject(@PathVariable Long projectId, @PathVariable Long teamId, @PathVariable TeamRole role) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        Optional<Team> teamOptional = teamRepository.findById(teamId);
        System.out.println(projectOptional);
        if (projectOptional.isPresent() && teamOptional.isPresent()) {
            Project project = projectOptional.get();
            Team team = teamOptional.get();

            // Check if the team is already added to the project
            boolean isTeamAdded = project.getTeams().stream()
                    .anyMatch(existingTeam -> existingTeam.getId().equals(teamId));

            if (isTeamAdded) {
                return ResponseEntity.badRequest().body(false);
            }

            // Add the team to the project
            project.getTeams().add(team);
            // Set the role for the team in the project
            this.setTeamRoleInProject(project, team, role);

            projectRepository.save(project);

            return ResponseEntity.ok(true);
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    }

    // A rectifier nahit l ligne lekhraneya
    private void setTeamRoleInProject(Project project, Team team, TeamRole role) {
        ProjectTeam projectTeam = new ProjectTeam();
        projectTeam.setProject(project);
        projectTeam.setTeam(team);
        projectTeam.setRole(role);

        project.getProjectTeams().add(projectTeam);
    }

/*
    @GetMapping("get_project_by_user/{userId}")
    public ResponseEntity<List<ProjectDTO>> getProjectsByUserId(@PathVariable Long userId) {
        List<Project> projects = this.projectRepository.findByUserId(userId);
        List<ProjectDTO> user_projects=new ArrayList<>();
        for(int i=0;i<projects.size();i++){
            user_projects.add(this.convertToDTO(projects.get(i)));

        }
        return ResponseEntity.ok(user_projects);
    }*//*
@GetMapping("get_project_by_user/{userId}")
public List<ProjectDTO> getProjectsByUserId(@PathVariable Long userId) {
    List<Project> projects = projectRepository.findByUserId(userId);
    List<ProjectDTO> projectDTOs = new ArrayList<>();

    for (Project project : projects) {
        ProjectDTO projectDTO = convertToDTO(project);
        List<UserRole> userRoles = userRoleRepository.findByProjectIdAndUserId(project.getId(), userId);
        List<UserRoleDTO> userRoleDTOs = new ArrayList<>();

        for (UserRole userRole : userRoles) {
            UserRoleDTO userRoleDTO = new UserRoleDTO();
            userRoleDTO.setRoleId(userRole.getId());
            userRoleDTO.setProjectId(userRole.getProject().getId());
            userRoleDTO.setUserId(userRole.getUser().getId());
            userRoleDTO.setRole(userRole.getRole().toString());
            userRoleDTOs.add(userRoleDTO);
        }

        projectDTO.setUserRoles(userRoleDTOs);
        projectDTOs.add(projectDTO);
    }

    return projectDTOs;
}
*/





}



