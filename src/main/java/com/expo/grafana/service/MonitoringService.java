package com.expo.grafana.service;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class MonitoringService {
    private final RestTemplate restTemplate;
    public MonitoringService() {
        this.restTemplate = new RestTemplate(clientHttpRequestFactory());
    }
    public String checkMetric(String metric) {
        String prometheusUrl = "http://prometheus-api/check";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(prometheusUrl)
                .queryParam("metric", metric);
        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, String.class);
        if (response.getStatusCodeValue() == 200) {
            return "Metric is available: " + metric;
        } else {
            return "Metric not found in Prometheus: " + metric;
        }
    }
    public String checkDeployment(String ip, String port) {
        String url = "http://localhost:8080/api/prometheus/query/check_deployment";
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                .queryParam("ip", ip)
                .queryParam("port", port);
        ResponseEntity<DeploymentResponse> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, null, DeploymentResponse.class);
        if (response.getStatusCodeValue() == 200) {
            return "Deployment: " + response.getBody().getDeployment();
        } else {
            return "Error checking deployment";
        }
    }
    private ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        return factory;
    }
    public Object get() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    static class DeploymentResponse {
        private String Deployment;
        public String getDeployment() {
            return Deployment;
        }
        public void setDeployment(String deployment) {
            Deployment = deployment;
        }
    }
    public void importDashboard(String dashboardTitle) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'importDashboard'");
    }
   
}