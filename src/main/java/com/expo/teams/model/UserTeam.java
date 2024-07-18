    package com.expo.teams.model;


    import com.expo.security.model.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

    @Entity
    @Table(name = "user_teams")
    @AllArgsConstructor
    @Setter
    @Getter
    @Builder
    public class UserTeam {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "user_id")
        private User user;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "team_id")
        private Team team;



    }
