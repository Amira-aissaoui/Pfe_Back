package com.expo.project.repo;

import com.expo.project.model.Project;
import com.expo.project.model.ProjectDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Project findProjectById(Integer id);
    Project findByProjectName(String projectName);
    @Query("SELECT p FROM Project p JOIN p.users u WHERE u.id = :userId")
    List<Project> findByUserId(Long userId);
    List<Project> findAllByVisibility(String visibility);


}
