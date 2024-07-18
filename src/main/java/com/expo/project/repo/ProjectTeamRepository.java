package com.expo.project.repo;

import com.expo.project.model.Project;
import com.expo.project.model.ProjectTeam;
import com.expo.teams.model.Team;
import com.expo.teams.model.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectTeamRepository extends JpaRepository<Project, Long> {
    @Query("SELECT pt FROM ProjectTeam pt WHERE pt.project.id = :projectId AND pt.team.id = :teamId")
    Optional<ProjectTeam> findByProjectIdAndTeamId(@Param("projectId") Long projectId, @Param("teamId") Long teamId);
    @Query("SELECT pt.role FROM ProjectTeam pt WHERE pt.project.id = :projectId AND pt.team.id = :teamId")
    TeamRole findRoleByProjectAndTeam(@Param("projectId") Long projectId, @Param("teamId") Long teamId);
    Optional<Project> findById(Long projectId);

    void deleteProjectsById(Long projectId);
    @Query("SELECT pt FROM ProjectTeam pt WHERE pt.team = ?1")
    List<ProjectTeam> findByTeam(Team team);


}
