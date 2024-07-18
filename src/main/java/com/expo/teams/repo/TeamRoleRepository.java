package com.expo.teams.repo;

import com.expo.project.model.ProjectTeam;
import com.expo.security.model.UserRole;
import com.expo.teams.model.TeamDTO;
import com.expo.teams.model.TeamRole;
import com.expo.teams.model.TeamRoleDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRoleRepository extends JpaRepository<ProjectTeam, Long> {

    @Query("SELECT new com.expo.teams.model.TeamRoleDTO(t.project.id, t.team.id, t.role) FROM ProjectTeam t WHERE t.team.id = :teamId")
    List<TeamRoleDTO> getTeamRolesByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT new com.expo.teams.model.TeamRoleDTO(t.project.id, t.team.id, t.role) FROM ProjectTeam t WHERE t.team.id = :teamId AND t.project.id = :projectId")
    TeamRoleDTO getTeamRoleByTeamIdAndProjectId(@Param("teamId") Long teamId, @Param("projectId") Long projectId);



}
