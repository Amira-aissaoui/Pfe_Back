package com.expo.grafana.model;

import com.expo.grafana.service.ApiKeyUpdater;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

public class DashboardManager {
    private String apiKey;
    @Value("${grafana.url}")

    private String grafanaUrl;
    
    private  final ApiKeyUpdater apikeyUpdate;


    public DashboardManager(String apiKey, String grafanaUrl, ApiKeyUpdater apikeyUpdate) {
        this.apiKey = apiKey;
        this.grafanaUrl = grafanaUrl;
        this.apikeyUpdate = apikeyUpdate;

    }
    @PostConstruct
    public void initialize() {
        this.apiKey = apikeyUpdate.getAPIKey();
    }


    @Entity
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Dashboard {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;


        public Dashboard() {

        }

    }
}
