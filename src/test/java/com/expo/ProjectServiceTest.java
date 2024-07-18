package com.expo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.expo.project.model.Project;
import com.expo.project.repo.ProjectRepository;
import com.expo.project.service.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveProject_ShouldSaveProject() {
        // Given
        Project project = new Project();

        // When
        projectService.saveProject(project);

        // Then
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    void getProjectById_ShouldReturnProject() {
        // Given
        Integer projectId = 1;
        Project expectedProject = new Project();
        when(projectRepository.findById(Long.valueOf(projectId))).thenReturn(Optional.of(expectedProject));

        // When
        Project resultProject = projectService.getProjectById(projectId);

        // Then
        assertEquals(expectedProject, resultProject);
    }

    @Test
    void getProjectByName_ShouldReturnProject() {
        // Given
        String projectName = "Sample Project";
        Project expectedProject = new Project();
        when(projectRepository.findByProjectName(projectName)).thenReturn(expectedProject);

        // When
        Project resultProject = projectService.getProjectByName(projectName);

        // Then
        assertEquals(expectedProject, resultProject);
    }


    @Test
    void getAllProjects_ShouldReturnListOfProjects() {
        // Given
        Project project1 = new Project();
        Project project2 = new Project();
        List<Project> expectedProjects = Arrays.asList(project1, project2);
        when(projectRepository.findAll()).thenReturn(expectedProjects);

        // When
        List<Project> resultProjects = projectService.getAllProjects();

        // Then
        assertEquals(expectedProjects, resultProjects);
    }

}
