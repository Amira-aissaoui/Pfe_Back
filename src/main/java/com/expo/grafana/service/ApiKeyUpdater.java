    package com.expo.grafana.service;

    import com.expo.grafana.model.ApiKeyEntity;
    import com.expo.grafana.model.ApiKeyResponse;
    import com.expo.grafana.repo.ApiKeyRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.*;
    import org.springframework.http.client.support.BasicAuthenticationInterceptor;
    import org.springframework.stereotype.Service;
    import org.springframework.web.client.HttpClientErrorException;
    import org.springframework.web.client.RestTemplate;

    @Service
    public class ApiKeyUpdater {
        @Value("${grafana.url}")
        public String grafanaUrl;

        @Value("${grafana.username}")
        public String grafanaUsername;

        @Value("${grafana.password}")
        public String grafanaPassword;

        public String apiKey;
        private final ApiKeyRepository apiKeyRepository;

        @Autowired
        public ApiKeyUpdater(ApiKeyRepository apiKeyRepository) {
            this.apiKeyRepository = apiKeyRepository;
            //   apiKey = getAPIKey();
        }

        public String getAPIKey() {
            ApiKeyEntity apiKeyEntity = apiKeyRepository.findById(1L).orElse(null);
            if (apiKeyEntity != null) {
                System.out.println("here"+apiKeyEntity.getApiKey());
                return apiKeyEntity.getApiKey();
            }
            return null;
        }


        public String setAPIKey() {
            if (apiKey == null || apiKey.isEmpty()) {
                RestTemplate restTemplate = new RestTemplate();
                String url = grafanaUrl + "/api/auth/keys";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                String requestBody = "{\"name\":\"api-token\",\"role\":\"Admin\"}";
                HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
                restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(grafanaUsername, grafanaPassword));
                try {
                    ResponseEntity<ApiKeyResponse> response = restTemplate.postForEntity(url, request, ApiKeyResponse.class);
                    System.out.println(response);

                    if (response.getStatusCode() == HttpStatus.OK) {
                        apiKey = response.getBody().getKey();
                        saveAPIKeyToDatabase(apiKey);
                    } else {
                        //apiKey = getAPIKey();
                        System.out.println("Failed to fetch/generate API key. Status code: " + response.getStatusCode());
                    }
                } catch (HttpClientErrorException e) {
                    if (e.getStatusCode() == HttpStatus.CONFLICT) {
                        apiKey = getAPIKey();
                    }
                }
            }
            return apiKey;
        }


        private void saveAPIKeyToDatabase(String apiKey) {
            ApiKeyEntity apiKeyEntity = apiKeyRepository.findById(1L).orElse(new ApiKeyEntity());
            apiKeyEntity.setId(1L);
            apiKeyEntity.setApiKey(apiKey);
            apiKeyRepository.save(apiKeyEntity);
        }
    }