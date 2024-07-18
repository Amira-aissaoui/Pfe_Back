package com.expo;

import static com.expo.security.model.Role.*;

import java.nio.file.Files;
import java.nio.file.Path;

import com.expo.security.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

import com.expo.grafana.service.ApiKeyUpdater;
import com.expo.prometheus.service.AlertFileGenerator;
import com.expo.prometheus.service.PrometheusConfigFileGenerator;
import com.expo.prometheus.service.RuleFileGenerator;
import com.expo.security.model.AuthenticationRequest;
import com.expo.security.model.AuthenticationResponse;
import com.expo.security.model.RegisterRequest;
import com.expo.security.service.AuthenticationService;
@SpringBootApplication(exclude = CompositeMeterRegistryAutoConfiguration.class)
public class ExpoApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(ExpoApplication.class, args);
	}

	@Value("${prometheus.server.url}")
	private String prometheusServerUrl;
	@Value("${alertmanager.config.path}")
	private String alertManagerConfigPath;
	@Value("${prometheus.config.path}")
	private String prometheusConfigPath;

	@Bean
	public CommandLineRunner commandLineRunner(AuthenticationService authenticationService) {
		return args -> {
			RegisterRequest admin = new RegisterRequest();

			admin.setFirstname("admin");
			admin.setLastname("admin");
			admin.setEmail("admin");
			admin.setPassword("admin");
			admin.setRole(ADMIN);

			try {
				AuthenticationResponse response = authenticationService.register(admin);
				System.out.println("Admin token: " + response.getAccessToken());
			} catch (Exception e) {
				System.out.println("Error during registration: " + e.getMessage());
				// Create a new AuthenticationRequest instance with email and password
				AuthenticationRequest request = AuthenticationRequest.builder()
						.email(admin.getEmail())
						.password(admin.getPassword())
						.build();
				AuthenticationResponse response = authenticationService.authenticate(request);
				System.out.println("Admin token: " + response.getAccessToken());
				System.out.println("Working Directory: " + System.getProperty("user.dir"));

			}
		};
	}

	@Bean
	public CommandLineRunner initializeFiles(AuthenticationService authenticationService, AlertFileGenerator alertFileGenerator, RuleFileGenerator ruleFileGenerator, PrometheusConfigFileGenerator prometheusConfigFileGenerator) {
		return args -> {

			String ruleFilePath = prometheusConfigPath+"/alert.rules.yml";
			String prometheusFilePath = prometheusConfigPath+"/prometheus.yml";
			String alertFilePath = alertManagerConfigPath+"/alertmanager.yml";

			if (!Files.exists(Path.of(alertFilePath))) {
				System.err.println("Alert file does not exist. Generating the Alert File.");
				alertFileGenerator.generateConfigFile();
			}
			if (!Files.exists(Path.of(prometheusFilePath))) {
				System.err.println("Prometheus config file does not exist. Generating the Prometheus Config File.");
				prometheusConfigFileGenerator.generateConfigFile();
			}
			if (!Files.exists(Path.of(ruleFilePath))) {
				System.err.println("Rule file does not exist. Generating the Rule File.");
				ruleFileGenerator.generateRuleFile();
			}
		};
	}
	@Bean
	public CommandLineRunner generateAPIKeyGrafana(ApiKeyUpdater apiKeyUpdater) {
		return args -> {

			String apiKey = apiKeyUpdater.setAPIKey();


			// Use the apiKey as needed
			if (apiKey != null && !apiKey.isEmpty()) {
				System.out.println("Generated/Fetched API key: " + apiKey);

			} else {
				System.out.println("Failed to generate/fetch API key.");
			}
		};
	}
/*	@Bean
	public CommandLineRunner addDataSource(PrometheusConfig prometheusConfig) {
		return args -> {

			prometheusConfig.addPrometheus("Prometheus",prometheusServerUrl);
		};
	}*/





}
