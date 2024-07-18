package com.expo.grafana.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.expo.grafana.model.ApiKeyResponse;

@RestController
@RequestMapping("/api/grafana_api")
@CrossOrigin(origins = "http://localhost:4200/")
public class GrafanaAPIController {

    @Value("${grafana.url}")
    private String grafanaUrl;

    @Value("${grafana.username}")
    private String grafanaUsername;

    @Value("${grafana.password}")
    private String grafanaPassword;

    @GetMapping("/generate-api-key")
    public ResponseEntity<String> generateApiKey() {
        RestTemplate restTemplate = new RestTemplate();

        String url = grafanaUrl + "/api/auth/keys";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = "{\"name\":\"api-token\",\"role\":\"Admin\"}";
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(grafanaUsername, grafanaPassword));

        try {
            ResponseEntity<ApiKeyResponse> response = restTemplate.postForEntity(url, request, ApiKeyResponse.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                String apiKey = response.getBody().getKey();
                return ResponseEntity.ok(apiKey);
            } else {
                System.out.println("Failed to generate API key. Status code: " + response.getStatusCode());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate API key.");
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                System.out.println("API key already exists. Fetching existing API key...");
                String existingApiKey = fetchExistingApiKey();
                if (existingApiKey == null) {
                    System.out.println("No existing API keys found. Generating a new API key...");
                    ResponseEntity<String> newApiKeyResponse = generateApiKey(); // Recursively call the method to generate a new API key
                    return newApiKeyResponse;
                } else {
                    return ResponseEntity.ok(existingApiKey);
                }
            }
            throw e;
        }
    }

    private String fetchExistingApiKey() {
        RestTemplate restTemplate = new RestTemplate();
        String url = grafanaUrl + "/api/auth/keys";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(headers);
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(grafanaUsername, grafanaPassword));

        try {
            ResponseEntity<ApiKeyResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    ApiKeyResponse[].class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                ApiKeyResponse[] apiKeyResponses = response.getBody();
                if (apiKeyResponses != null && apiKeyResponses.length > 0) {
                    String existingApiKey = apiKeyResponses[0].getKey();
                    return existingApiKey;
                } else {
                    System.out.println("No existing API keys found.");
                    return null;
                }
            } else {
                System.out.println("Failed to fetch existing API key. Status code: " + response.getStatusCode());
                return null;
            }
        } catch (HttpClientErrorException e) {
            System.out.println("Error while fetching existing API key: " + e.getMessage());
            return null;
        }
    }
}