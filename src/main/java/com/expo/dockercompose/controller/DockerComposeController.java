package com.expo.dockercompose.controller;
import java.io.IOException;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RequestMapping("/api/docker")
@RestController
@CrossOrigin(origins = "http://localhost:4200/")
public class DockerComposeController {

    @GetMapping("/startDockerCompose")

    public String startDockerCompose() {

        try {

            ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", "-f", "src/main/resources/docker-compose-servers/docker-compose.yml", "up", "-d");

            Process process = processBuilder.start();

            int exitCode = process.waitFor();



            if (exitCode == 0) {

                return "Docker Compose started successfully.";

            } else {

                return "Failed to start Docker Compose.";

            }

        } catch (IOException | InterruptedException e) {

            e.printStackTrace();

            return "An error occurred while starting Docker Compose.";

        }

    }

}
