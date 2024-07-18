package com.expo.project.service;

import com.expo.project.model.Project;
import com.expo.project.model.ProjectTeam;
import com.expo.project.repo.ProjectRepository;
import com.expo.project.repo.ProjectTeamRepository;
import io.swagger.models.auth.In;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectTeamRepository projectTeamRepository;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectTeamRepository projectTeamRepository) {
        this.projectRepository = projectRepository;
        this.projectTeamRepository = projectTeamRepository;
    }


    @Override
    public void saveProject(Project project) {
        projectRepository.save(project);
    }

    @Override
    public Project getProjectById(Integer id) {
        Optional<Project> project = projectRepository.findById(Long.valueOf(id));
        return project.orElse(null);
    }

    @Override
    public Project getProjectByName(String projectName) {
        Optional<Project> project = Optional.ofNullable(projectRepository.findByProjectName(projectName));
        return project.orElse(null);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public List<Map<String, String>> getIpAddressesByProjectId(Long projectId) {
        List<Map<String, String>> result = new ArrayList<>();
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            String[] ipAddresses = project.getIpAddresses().split(",");
            for (String ipAddress : ipAddresses) {
                String[] parts = ipAddress.split(":");
                if (parts.length == 2) {
                    String address = parts[0].trim();
                    String port = parts[1].trim();
                    Map<String, String> addressAndPort = new HashMap<>();
                    addressAndPort.put("address", address);
                    addressAndPort.put("port", port);
                    result.add(addressAndPort);
                }
            }
        }
        return result;
    }
    @Override
    public void deleteProject(Long projectId) {
        Optional<Project> projectOptional = projectRepository.findById(projectId);
        //Optional<Project> projectTeamOptional = projectTeamRepository.findById(projectId);

      //  projectTeamOptional.ifPresent(projectTeam -> projectTeamRepository.delete(projectTeam));
        projectOptional.ifPresent(project -> projectRepository.delete(project));
    }
    @PersistenceContext
    private EntityManager entityManager;

    public boolean changeprojectinfo(Long projectId, String fieldName, String oldValue, String newValue) {
        Project project = entityManager.find(Project.class, projectId);
        if (project == null) {
            System.err.println("Project with ID " + projectId + " not found.");
            return false;
        }

        if ("msnames".equalsIgnoreCase(fieldName)) {
            List<String> msnamesList = new ArrayList<>(Arrays.asList(project.getMsnames().split(",")));
            int index = msnamesList.indexOf(oldValue);
            if (index != -1) {
                msnamesList.set(index, newValue);
                project.setMsnames(String.join(",", msnamesList));
            } else {
                System.err.println("Old value '" + oldValue + "' not found in msnames.");
                return false;
            }
        } else if ("ipAddresses".equalsIgnoreCase(fieldName)) {
            List<String> ipAddressesList = new ArrayList<>(Arrays.asList(project.getIpAddresses().split(",")));
            int index = ipAddressesList.indexOf(oldValue);
            if (index != -1) {
                ipAddressesList.set(index, newValue);
                project.setIpAddresses(String.join(",", ipAddressesList));
            } else {
                System.err.println("Old value '" + oldValue + "' not found in ipAddresses.");
                return false;
            }
        } else {
            System.err.println("Invalid field name. Field name should be either 'msnames' or 'ipAddresses'.");
            return false;
        }

        entityManager.merge(project);
        System.out.println("Project field '" + fieldName + "' updated successfully.");
        return true;
    }
    public int getInstanceTagByInstance(String ipaddr, Long projectID){
        Project project = entityManager.find(Project.class, projectID);
        if (project == null) {
            System.err.println("Project with ID " + projectID + " not found.");
            return -1;
        }
        if((project.getIpAddresses().split(":").length) ==2){
            System.out.println("hereeeee");
            return 1;
        }
        else{
            System.out.println("hereeeee2"+project.getIpAddresses());

            List<String> ipAddressesList = new ArrayList<>(Arrays.asList(project.getIpAddresses().split(",")));
            int index = ipAddressesList.indexOf(ipaddr);
            return index+1;

        }
    }




}
