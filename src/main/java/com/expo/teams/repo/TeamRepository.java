package com.expo.teams.repo;

import com.expo.security.model.User;
import com.expo.teams.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByTeamName(String teamName);

    @Query("SELECT t FROM Team t JOIN FETCH t.projects p JOIN t.users u WHERE u.id = :userId")
    List<Team> findTeamsByUserId(@Param("userId") Long userId);



}

