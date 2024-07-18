package com.expo.grafana.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.annotation.PostConstruct;

@Service
public class GrafanaClient {

    @Value("${grafana.url}")
    private String grafanaUrl;

    private String apiKey;
    private final ApiKeyUpdater apikeyUpdate;
    private final ObjectMapper objectMapper;

    public GrafanaClient(ApiKeyUpdater apikeyUpdate, ObjectMapper objectMapper) {
        this.apikeyUpdate = apikeyUpdate;
        this.objectMapper = objectMapper;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }



    @PostConstruct
    public void initialize() {
        this.apiKey = apikeyUpdate.getAPIKey();
    }

    public String GetDashboard(String dashboardTitle) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        System.out.print("this.apiKey"+this.apiKey);
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle+ "&type=dash-db", HttpMethod.GET, requestEntity, String.class);

        if (searchResponse.getStatusCodeValue() != 200) {
            throw new RuntimeException("Failed to retrieve dashboard search result: " + searchResponse.getBody());
        }
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found: " + dashboardTitle);
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        if (dashboardResponse.getStatusCodeValue() != 200) {
            throw new RuntimeException("Failed to retrieve dashboard: " + dashboardResponse.getBody());
        }
        String dashboardJson = dashboardResponse.getBody();
        return dashboardJson;
    }
    public boolean doesDashboardExist(String dashboardTitle) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(
                grafanaUrl + "api/search?query=" + dashboardTitle + "&type=dash-db",
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        if (searchResponse.getStatusCodeValue() == 200) {
            String searchResultJson = searchResponse.getBody();
            JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
            return searchResultNode.size() > 0;
        } else {
            throw new RuntimeException("Failed to retrieve dashboard search result: " + searchResponse.getBody());
        }
    }


    private HttpEntity<String> getHeaderHttp(){
        HttpHeaders headers = new HttpHeaders();
        //System.out.println("apiKey"+apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        return requestEntity;

    }

    public void createDashboard(String jsonPayload) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboard = objectMapper.readValue(jsonPayload, ObjectNode.class);
       // dashboard.put("overwrite", true);

        ObjectNode requestPayload = objectMapper.createObjectNode();
        requestPayload.set("dashboard", dashboard);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestPayload.toString(), headers);

        RestTemplate restTemplate = new RestTemplate();

        restTemplate.exchange(grafanaUrl + "api/dashboards/db/", HttpMethod.POST, requestEntity, String.class);
    }

    public String getDashboardIdByTitle(String dashboardTitle) throws IOException {
        HttpEntity<String> requestEntity=this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(restTemplate);
        ResponseEntity<String> response = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle + "&type=dash-db", HttpMethod.GET, requestEntity, String.class);
      //System.out.println("l response"+response);

        JsonNode root = new ObjectMapper().readTree(response.getBody());
      //System.out.println(root);

        JsonNode firstResult = root.get(0);
      //System.out.println(firstResult);

        String dashboardId = firstResult.get("id").asText();
      //System.out.println("id"+dashboardId);

        return dashboardId;
}
    public String getDashboardUidByTitle(String dashboardTitle) throws IOException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(dashboardTitle);

        ResponseEntity<String> response = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle.toString() + "&type=dash-db", HttpMethod.GET, requestEntity, String.class);
        System.out.println(response);
        String responseBody = response.getBody();
        try {
            JsonNode root = new ObjectMapper().readTree(responseBody);
            JsonNode firstResult = root.get(0);
            System.out.println(firstResult);

            String dashboardUid = firstResult.get("uid").asText();
            return dashboardUid;
        } catch (IOException e) {
            // Print the exception message or stack trace to identify the cause
            e.printStackTrace();
            throw e; // Rethrow the exception or handle it accordingly
        }
    }



    public void deleteDashboard(String dashboardTitle) {
        // Fetch the dashboard ID by title from Grafana
        HttpEntity<String> requestEntity = getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode;
        try {
            searchResultNode = new ObjectMapper().readTree(searchResultJson);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing search result JSON: " + e.getMessage(), e);
        }
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // Delete the dashboard
        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> deleteRequestEntity = new HttpEntity<>(deleteHeaders);
        ResponseEntity<String> deleteResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.DELETE, deleteRequestEntity, String.class);

        // Check delete was mrigl wale
        if (deleteResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard deleted successfully");
        } else {
            throw new RuntimeException("Dashboard delete failed: " + deleteResponse.getStatusCodeValue() + " " + deleteResponse.getBody());
        }
    }


    //----------refresh

    public void modifyDashboard(String dashboardTitle, String newTitle, String refresh,String timeFrom,String timeTo,String timeRange) throws JsonProcessingException {
        //  dashboard JSON
        String dashboardJson = this.GetDashboard(dashboardTitle);

        // conversion JSON string to a JSON object
        ObjectMapper mapper = new ObjectMapper();
        JsonNode dashboardNode = mapper.readTree(dashboardJson).get("dashboard");

        // modification du titre
        if (newTitle != null && !newTitle.isEmpty()) {
            ((ObjectNode) dashboardNode).put("title", newTitle);
        }

        // Modify the refresh interval
        if (refresh != null && !refresh.isEmpty()) {
            ((ObjectNode) dashboardNode).put("refresh", refresh+"s");
        }
        if (timeFrom != null && !timeFrom.isEmpty() && timeTo != null && !timeTo.isEmpty()) {
            ObjectNode timeNode = mapper.createObjectNode();
            timeNode.put("from", timeFrom);
            timeNode.put("to", timeTo);
            ((ObjectNode) dashboardNode).set("time", timeNode);
        }

        if (timeRange != null && !timeRange.isEmpty()) {
            ObjectNode timeNode = mapper.createObjectNode();
            timeNode.put("from", "now-"+timeRange);
            timeNode.put("to", "now");

            ((ObjectNode) dashboardNode).set("time", timeNode);
        }

        // Convert the modified JSON to string + update dashboard
        String modifiedDashboardJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode);
        System.out.println(modifiedDashboardJson);

        this.updateDashboard(modifiedDashboardJson);
    }




//--------------------------------------------------------------------------------------------------------

//---------------------code callender


public void dashboardCalendar(String dashboardTitle, LocalDate startDate, LocalDate endDate) throws JsonProcessingException {
    // Get the dashboard JSON
    String dashboardJson = this.GetDashboard(dashboardTitle);

    // Convert the JSON string to a JSON object
    JsonNode dashboardNode = objectMapper.readTree(dashboardJson).get("dashboard");

    // Check if dashboardNode is null
    if (dashboardNode == null) {
        throw new RuntimeException("dashboardNode is null");
    }

    // Get the panels node
    JsonNode panelsNode = dashboardNode.get("panels");
    if (panelsNode == null || panelsNode.size() == 0) {
        throw new RuntimeException("panelsNode is null or empty");
    }

    // Iterate through each panel to check if the date range overlaps with the panel's time range
    for (JsonNode panelNode : panelsNode) {
        JsonNode timeRangeNode = panelNode.get("timeRange");

        // Check if timeRangeNode exists
        if (timeRangeNode != null) {
            // Get the time range from the panel data
            Instant timeFrom = Instant.parse(timeRangeNode.get("from").asText());
            Instant timeTo = Instant.parse(timeRangeNode.get("to").asText());

            // Check if the input date range overlaps with the time range in the panel data
            if (startDate.isAfter(timeFrom.atZone(ZoneId.systemDefault()).toLocalDate()) &&
                endDate.isBefore(timeTo.atZone(ZoneId.systemDefault()).toLocalDate())) {

                // If date range overlaps, update time range based on input date range
                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
                String timeFromJson = startDate.format(formatter) + "T00:00:00.000Z";
                String timeToJson = endDate.format(formatter) + "T23:59:59.999Z";
                ObjectNode timeNode = objectMapper.createObjectNode();
                timeNode.put("from", timeFromJson);
                timeNode.put("to", timeToJson);

                // Update dashboard with modified time range
                ((ObjectNode) dashboardNode).set("time", timeNode);

                // Convert the modified JSON to string
                String modifiedDashboardJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode);

                // Return or update dashboard based on your requirement
                // For example:
                System.out.println(modifiedDashboardJson);
                // Or call a method to update the dashboard
                // updateDashboard(modifiedDashboardJson);

                return; // If you only need to process one suitable dashboard
            }
        }
    }

    // If no suitable dashboard is found
    System.out.println("No suitable dashboard found for the given date range and title.");
}





















    public void updateDashboard(String dashboardJson) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        String url = grafanaUrl + "api/dashboards/db";
        HttpHeaders headers = getHeaders();
        System.out.println("dashboardJson"+dashboardJson);

      //  HttpEntity<String> requestEntity = new HttpEntity<String>("{\"dashboard\":" + dashboardJson + "}", headers);
        HttpEntity<String> requestEntity = new HttpEntity<String>( dashboardJson , headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to update dashboard: " + response.getBody());
        }
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        return headers;
    }

    public JsonNode getDashboardByTitle(String dashboardTitle) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        System.out.println(response);
        JsonNode root = new ObjectMapper().readTree(response.getBody());
        if (root.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }

        for (JsonNode result : root) {
            JsonNode titleNode = result.get("title");
            if (titleNode != null && dashboardTitle.equals(titleNode.asText())) {
                return result;
            }
        }

        throw new RuntimeException("Dashboard not found");
    }


    // f actia temchili .get("dashboard").get(panels) f pc mteei .get("rows").get(0).get("panels") c donc à verifier (tested 06 Mai 2023)

    public List<JsonNode> GetPanels(String dashboardTitle) throws JsonProcessingException {
        String dashboardJson = this.GetDashboard(dashboardTitle);
        //System.out.println(dashboardJson);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dashboardNode = objectMapper.readTree(dashboardJson);
        List<JsonNode> panels = new ArrayList<>();
      //  System.out.println(dashboardNode.get("dashboard").get("rows").get(0).get("panels"));
        JsonNode panelNodes;
        if( dashboardNode.get("dashboard").get("panels").isEmpty()) {
            panelNodes=dashboardNode.get("dashboard").get("rows").get(0).get("panels");
        }
        else{
            panelNodes=dashboardNode.get("dashboard").get("panels");

        }
        for (JsonNode panelNode : panelNodes) {
     //   for (JsonNode panelNode : dashboardNode.get("dashboard").get("rows").get(0).get("panels")) {
            panels.add(panelNode);
        }


        System.out.println("OKKKK");
        System.out.println(panels);

        return panels;
    }






    public List<String> getAllPanelIds(String dashboardTitle) throws JsonProcessingException {
        String dashboardJson = this.GetDashboard(dashboardTitle);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dashboardNode = objectMapper.readTree(dashboardJson);
        List<String> panelIds = new ArrayList<>();
       // System.out.println("get all panels"+dashboardNode.path("dashboard").path("rows").path(0).has("panels"));
        System.out.println("get all panels"+dashboardNode.path("dashboard").has("panels"));

        if (dashboardNode.path("dashboard").has("panels")) {
            JsonNode panelsNode = dashboardNode.path("dashboard").path("panels");
            if (panelsNode.isArray()) {
                for (JsonNode panelNode : panelsNode) {
                    panelIds.add(panelNode.get("id").asText());
                    System.out.println(panelNode);



                }
            }
        }
        else{
                JsonNode panelsNode = dashboardNode.path("dashboard").get("rows").get(0).path("panels");
                if (panelsNode.isArray()) {
                    for (JsonNode panelNode : panelsNode) {
                        panelIds.add(panelNode.get("id").asText());
                        System.out.println(panelNode);
                    }
                }

        }

        return panelIds;
    }


    public List<String> getAllPanelIdsByTag(String dashboardTitle,String tag) throws JsonProcessingException {
        System.out.println("l tag"+tag);

        String dashboardJson = this.GetDashboard(dashboardTitle);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode dashboardNode = objectMapper.readTree(dashboardJson);
        List<String> panelIds = new ArrayList<>();
        System.out.println("get all panels"+dashboardNode.path("dashboard").path("panels"));

        if (dashboardNode.path("dashboard").has("panels")) {
            JsonNode panelsNode = dashboardNode.path("dashboard").path("panels");
            System.out.println(panelsNode);

            if (panelsNode.isArray()) {
                for (JsonNode panelNode : panelsNode) {
                    System.out.println("lena1"+panelNode.get("targets").get(0).get("tags").asText());
                    if(!panelNode.get("targets").get(0).get("tags").asText().isEmpty()){
                        String panelTag = (panelNode.get("targets").get(0).get("tags").asText());
                        System.out.println("compraison f node"+panelTag.equals(tag));
                        if(panelTag.equals(tag)){
                            panelIds.add(panelNode.get("id").asText());

                        }
                    }
                }
            }
        }
        else{
            JsonNode panelsNode = dashboardNode.path("dashboard").get("rows").get(0).path("panels");
            if (panelsNode.isArray()) {
                for (JsonNode panelNode : panelsNode) {
                  //  System.out.println(panelNode);
                    System.out.println("lena2"+panelNode.get("targets").get(0).get("tags").asText());

                    if(!panelNode.get("targets").get(0).get("tags").asText().isEmpty() ){
                        String panelTag = (panelNode.get("targets").get(0).get("tags").asText());
                        System.out.println("compraison f node"+panelTag.equals(tag));

                        if(panelTag.equals(tag)){
                            panelIds.add(panelNode.get("id").asText());

                        }
                    }
            }
            }


        }

        return panelIds;
    }




//zedet exper

public String getPanelExprByPanelId(String dashboardTitle, String panelId) throws JsonProcessingException {
    String dashboardJson = this.GetDashboard(dashboardTitle);
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode dashboardNode = objectMapper.readTree(dashboardJson);
    String panelExpr = null;

    if (dashboardNode.path("dashboard").has("panels")) {
        JsonNode panelsNode = dashboardNode.path("dashboard").path("panels");

        if (panelsNode.isArray()) {
            for (JsonNode panelNode : panelsNode) {
                if (panelNode.get("id").asText().equals(panelId)) {
                    panelExpr = panelNode.get("targets").get(0).get("expr").asText();
                    break;
                }
            }
        }
    } else {
        JsonNode panelsNode = dashboardNode.path("dashboard").get("rows").get(0).path("panels");

        if (panelsNode.isArray()) {
            for (JsonNode panelNode : panelsNode) {
                if (panelNode.get("id").asText().equals(panelId)) {
                    panelExpr = panelNode.get("targets").get(0).get("expr").asText();
                    break;
                }
            }
        }
    }

    return panelExpr;
}





}

