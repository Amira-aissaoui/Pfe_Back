package com.expo.grafana.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;


//kiff kiff à verifier le path du panels here
// f actia temchili .get("dashboard").get(panels) f pc mteei .get("rows").get(0).get("panels") c donc à verifier aalech (tested 06 Mai 2023)
//ps il y'avait un changement de la version du grafana que j'utilise donc peut etre l json à générer tbadlet l format mteeo
@CrossOrigin(origins = "*")


@Service
public class PanelClient {
    @Value("${grafana.url}")
    private String grafanaUrl;

    private String apiKey;
    private final ApiKeyUpdater apikeyUpdate;

    @Autowired
    private GrafanaClient grafanaClient;

    public PanelClient(ApiKeyUpdater apikeyUpdate){
        this.apikeyUpdate = apikeyUpdate;
    }

    @PostConstruct
    public void initialize() {
        this.apiKey = apikeyUpdate.getAPIKey();
    }


    private HttpEntity<String> getHeaderHttp() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(headers);
    }

    public void addPanel(String dashboardTitle, String panelTitle, String targetExpr, String chart,Integer id,Integer tag) throws JsonProcessingException {
        JsonNode dashboardPanelNode;
        // Searching for the dashboard
        HttpEntity<String> requestEntity = getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // Retrieve the current dashboard JSON
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId+"?overwrite=true", HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();

        // Update the dashboard JSON with the new panel
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        // Check if the dashboard structure is valid

        System.out.println("dashboardNode"+dashboardNode);
        if (dashboardNode.path("dashboard").has("rows")) {
            System.out.println("d5al lena"+dashboardNode.path("dashboard").path("rows"));

            ArrayNode rowsNode = (ArrayNode) dashboardNode.path("dashboard").path("rows");
            ArrayNode panelsNode = (ArrayNode) rowsNode.get(0).path("panels");
            dashboardPanelNode = panelsNode;
        } else {
            System.out.println("d5al lena2"+dashboardNode.path("dashboard").path("panels"));

            dashboardPanelNode = dashboardNode.path("dashboard").path("panels");

        }

        // Create a new panel
        ObjectNode panelNode = objectMapper.createObjectNode();
        panelNode.put("title", panelTitle);


        panelNode.put("id", id);

        panelNode.put("type", chart);
        panelNode.put("datasource", "Prometheus");

        ArrayNode targetsNode = objectMapper.createArrayNode();
        ObjectNode targetNode = objectMapper.createObjectNode();
        targetNode.put("expr", targetExpr);
        targetNode.put("tags", tag);
        targetsNode.add(targetNode);

        panelNode.set("targets", targetsNode);

        // Add the panel to the panels array
        ((ArrayNode) dashboardPanelNode).add(panelNode);
        dashboardNode.put("overwrite", true);


        // Send the updated dashboard JSON to the server
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        System.out.println(dashboardNode);
        ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db", HttpMethod.POST, updateRequestEntity, String.class);

        // Check if the update was successful
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard updated successfully");


        } else {
            throw new RuntimeException("Dashboard update failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
        }
    }


    public void deletePanel(String dashboardTitle, String panelTitle) throws JsonProcessingException {
        // Fetch the dashboard ID by title from Grafana
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // Get the dashboard JSON and remove the panel
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        ArrayNode panelsNode;
        //updated
        if (dashboardNode.path("dashboard").has("rows")) {
            panelsNode =(ArrayNode) dashboardNode.path("dashboard").path("rows").get(0).path("panels");
            System.out.println("panelsnode1"+panelsNode);
        } else {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
            System.out.println("panelsnode2"+panelsNode);
        }


        System.out.println("panelsNode"+panelsNode);
        boolean panelDeleted = false;
        for (int i = 0; i < panelsNode.size(); i++) {
            JsonNode panelNode = panelsNode.get(i);
            if (panelNode.get("title").asText().equals(panelTitle)) {
                panelsNode.remove(i);
                panelDeleted = true;
                break;
            }
        }
        if (!panelDeleted) {
            throw new RuntimeException("Panel not found");
        }

        // Update the dashboard in Grafana
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db/", HttpMethod.POST, updateRequestEntity, String.class);

        // Check if the update was successful
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard updated successfully");
        } else {
            throw new RuntimeException("Dashboard update failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
        }
    }
    public void deletePanelById(String dashboardTitle, String panelId) throws JsonProcessingException {
        // Fetch the dashboard ID by title from Grafana
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // Get the dashboard JSON and remove the panel
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        System.out.println("dashboardNode"+dashboardNode);
        ArrayNode panelsNode;
        System.out.println("test1"+(ArrayNode) dashboardNode.path("dashboard").path("panels"));

        //updated

        if (dashboardNode.path("dashboard").has("rows")) {
            panelsNode =(ArrayNode) dashboardNode.path("dashboard").path("rows").get(0).path("panels");
            System.out.println("panelsnode1"+panelsNode);
        } else {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
            System.out.println("panelsnode2"+panelsNode);
        }


        System.out.println("panelsNode"+panelsNode);
        boolean panelDeleted = false;
        for (int i = 0; i < panelsNode.size(); i++) {
            JsonNode panelNode = panelsNode.get(i);
            if (panelNode.get("id").asText().equals(panelId)) {
                if (panelsNode.has(i)) {
                    panelsNode.remove(i);
                    panelDeleted = true;
                    break;
                }
            }
        }
        if (!panelDeleted) {
            throw new RuntimeException("Panel not found");
        }

        // Update the dashboard in Grafana
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db/", HttpMethod.POST, updateRequestEntity, String.class);

        // Check if the update was successful
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard updated successfully");
        } else {
            throw new RuntimeException("Dashboard update failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
        }
    }


    public void updatePanel(String dashboardTitle, String panelTitle, ObjectNode updatedPanel) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found");
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // get dashboard JSON from Grafana
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();

        // Update the dashboard JSON pour le new panel
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        ArrayNode panelsNode;

        if (dashboardNode.path("dashboard").path("panels").isArray()) {
            panelsNode =(ArrayNode) dashboardNode.path("dashboard").path("rows").get(0).path("panels");
        } else {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
        }

        for (JsonNode panelNode : panelsNode) {
            if (panelNode.path("title").asText().equals(panelTitle)) {
                ((ObjectNode) panelNode).setAll(updatedPanel);
                break;
            }
        }

        // Send the updated JSON to Grafana
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db/", HttpMethod.POST, updateRequestEntity, String.class);

        // Check if the update was successful
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard updated successfully");
        } else {
            throw new RuntimeException("Dashboard update failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
        }
    }





    public void modifyPanel(String dashboardTitle, int panelId, String newTitle,String newType, String newExper ) throws Exception {
        //  dashboard JSON
        String dashboardJson = grafanaClient.GetDashboard(dashboardTitle);

        // Find the panel to modify based on the panel ID

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        ArrayNode panelsNode;

        if (!dashboardNode.path("dashboard").path("panels").isArray()) {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("rows").get(0).path("panels");
        } else {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
        }


        JsonNode panelNode = null;
        for (JsonNode panel : panelsNode) {
            if (panel.get("id").asInt() == panelId) {
                panelNode = panel;
                System.out.println(panelNode); //OK

                break;
            }
        }

        if (panelNode == null) {
            throw new Exception("Panel with ID " + panelId + " not found in dashboard " + dashboardTitle);
        }

        if ((newTitle != null && !newTitle.isEmpty()) || newTitle.length()>0) {
            ((ObjectNode) panelNode).put("title", newTitle);
        }

        if ((newType != null && !newType.isEmpty() )|| newType.length()>0) {
            ((ObjectNode) panelNode).put("type", newType);
        }

        String modifiedDashboardJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode);
        System.out.println("modifiedDashboardJson"+modifiedDashboardJson);
        grafanaClient.updateDashboard(modifiedDashboardJson);

    }








    //Chnowa hedhaaaaaa ??
    //fiha bugg org.springframework.web.client.HttpClientErrorException$BadRequest: 400 Bad Request: "{"message":"Dashboard title cannot be empty","status":"empty-name"}"
    public void modifyPanelInstance(String dashboardTitle, int panelId, String newInstance) throws Exception {
        // Get the dashboard JSON
        String dashboardJson = grafanaClient.GetDashboard(dashboardTitle);

        // Find the panel to modify based on the panel ID
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        ArrayNode panelsNode;

        if (dashboardNode.path("dashboard").path("panels").isEmpty()) {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("rows").get(0).path("panels");
        } else {
            panelsNode = (ArrayNode) dashboardNode.path("dashboard").path("panels");
        }
        System.out.println("panelsnode"+panelsNode);
        JsonNode panelNode = null;
        for (JsonNode panel : panelsNode) {
            if (panel.get("id").asInt() == panelId) {
                panelNode = panel;
                System.out.println(panelNode); // OK
                break;
            }
        }

        if (panelNode == null) {
            throw new Exception("Panel with ID " + panelId + " not found in dashboard " + dashboardTitle);
        }
        System.out.println("panelnode"+panelsNode);
        // Check if the panel has targets
        if (panelNode.path("targets").isArray() && panelNode.path("targets").size() > 0) {
            // nbadel f the query expression to update the instance
            String expr = panelNode.path("targets").get(0).path("expr").asText();
            System.out.println("expr"+expr);
            String updatedExpr = expr.replaceFirst("instance=\"[^\"]+\"", "instance=\"" + newInstance + "\"");
            System.out.println("updatedExpr"+updatedExpr);

            ((ObjectNode) panelNode.path("targets").get(0)).put("expr", updatedExpr);
        } else {
            throw new Exception("Panel with ID " + panelId + " does not have any targets");
        }
        System.out.println("dashboardNode"+dashboardNode);

        String modifiedDashboardJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dashboardNode);
        grafanaClient.updateDashboard(modifiedDashboardJson);

    }










    // mazelt masta3malthech fl front
    public JsonNode getPanelByTitle(String dashboardTitle, String panelTitle) throws JsonProcessingException {
        HttpEntity<String> requestEntity = getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();

        // Search for the dashboard
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found with title: " + dashboardTitle);
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // dashboard JSON
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();

        JsonNode dashboardNode = new ObjectMapper().readTree(dashboardJson);
        JsonNode panelsNode = dashboardNode.get("dashboard").get("panels");
        for (JsonNode panelNode : panelsNode) {
            if (panelNode.get("title").asText().equals(panelTitle)) {
                return panelNode;
            }
        }

        throw new RuntimeException("Panel not found with title: " + panelTitle);
    }


    //normalement mrigl
    public JsonNode getPanelById(String panelId,String dashboardTitle) throws JsonProcessingException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();

        //  Wini l dashboard
        ResponseEntity<String> searchResponse = restTemplate.exchange(grafanaUrl + "api/search?query=" + dashboardTitle, HttpMethod.GET, requestEntity, String.class);
        String searchResultJson = searchResponse.getBody();
        JsonNode searchResultNode = new ObjectMapper().readTree(searchResultJson);
        if (searchResultNode.size() == 0) {
            throw new RuntimeException("Dashboard not found with title: " + dashboardTitle);
        }
        String dashboardId = searchResultNode.get(0).get("uid").asText();

        // jib l dashboard JSON
        ResponseEntity<String> dashboardResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardId, HttpMethod.GET, requestEntity, String.class);
        String dashboardJson = dashboardResponse.getBody();

        // wini l panel fl dashboard JSON
        JsonNode dashboardNode = new ObjectMapper().readTree(dashboardJson);
        JsonNode panelsNode;
                //= dashboardNode.get("dashboard").get("panels");


        if(dashboardNode.get("dashboard").get("panels").isEmpty())
        {
            panelsNode=dashboardNode.get("dashboard").get("rows").get(0).get("panels");
        }
        else {
            panelsNode=dashboardNode.get("dashboard").get("panels");

        }

        for (JsonNode panelNode : panelsNode) {
            if (panelNode.get("id").asText().equals(panelId)) {
             //   System.out.println(panelNode.toString());
                return panelNode;
            }
        }

        throw new RuntimeException("Panel not found with title: " + panelId);
    }
    public String getPanelIdByTitle(String dashboardTitle, String panelTitle) throws IOException {
        HttpEntity<String> requestEntity = this.getHeaderHttp();
        RestTemplate restTemplate = new RestTemplate();

        String dashboardUid = grafanaClient.getDashboardUidByTitle(dashboardTitle);

        // Search for l panels in the dashboard
        ResponseEntity<String> panelSearchResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/uid/" + dashboardUid, HttpMethod.GET, requestEntity, String.class);
        JsonNode dashboardJson = new ObjectMapper().readTree(panelSearchResponse.getBody());
        JsonNode panels = dashboardJson.at("/dashboard/panels");
        System.out.println(panels);

        for (JsonNode panel : panels) {
            if (panel.get("title").asText().equals(panelTitle)) {
                System.out.println(panel.get("id").asText());
                return panel.get("id").asText();
            }
        }
        throw new RuntimeException("Panel not found: " + panelTitle);
    }





    
    // FIXEEEEEEEEEEEEEEEEEEED
public void setFormat(String dashboardTitle, Integer panelId,String unit) throws Exception {
    // Get the dashboard JSON
    String dashboardJson = grafanaClient.GetDashboard(dashboardTitle);
    System.out.println(dashboardJson);

    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
    JsonNode panelsNode;

    if (dashboardNode.path("dashboard").path("panels").isEmpty()) {
        System.out.println("lena1");
        panelsNode = dashboardNode.path("dashboard").path("rows").get(0).path("panels");
    } else {
        System.out.println("lena2");
        panelsNode = dashboardNode.path("dashboard").path("panels");
    }

    ObjectNode panelNode = null;
    for (JsonNode panel : panelsNode) {
        System.out.println("panel" + panel);
        if (panel.get("id").asInt() == panelId) {
            panelNode = (ObjectNode) panel;
            System.out.println(panelNode); // OK
            break;
        }
    }

    if (panelNode == null) {
        throw new Exception("Panel with ID " + panelId + " not found in dashboard " + dashboardTitle);
    }

    // Check if fieldConfig exists in panelNode
    if (!panelNode.has("fieldConfig")) {
        ObjectNode fieldConfigNode = objectMapper.createObjectNode();
        panelNode.set("fieldConfig", fieldConfigNode);
    }

    // Check if defaults exists in fieldConfigNode
    ObjectNode fieldConfigNode = (ObjectNode) panelNode.path("fieldConfig");
    if (!fieldConfigNode.has("defaults")) {
        ObjectNode defaultsNode = objectMapper.createObjectNode();
        fieldConfigNode.set("defaults", defaultsNode);
    }

    ObjectNode defaultsNode = (ObjectNode) fieldConfigNode.path("defaults");
    defaultsNode.put("unit", unit);

    System.out.println("dashboardNode" + dashboardNode);

    RestTemplate restTemplate = new RestTemplate();

    // Send the updated dashboard JSON to the server
    HttpHeaders updateHeaders = new HttpHeaders();
    updateHeaders.setContentType(MediaType.APPLICATION_JSON);
    updateHeaders.set("Authorization", "Bearer " + apiKey);
    HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
    System.out.println(dashboardNode);
    ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db", HttpMethod.POST, updateRequestEntity, String.class);

    // Check if the update was successful
    if (updateResponse.getStatusCode().is2xxSuccessful()) {
        System.out.println("Dashboard updated successfully");
    } else {
        throw new RuntimeException("Dashboard update failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
    }
}

    public void setPanelText(String dashboardTitle, Integer panelId) throws Exception {
        // Get the dashboard JSON
        String dashboardJson = grafanaClient.GetDashboard(dashboardTitle);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        JsonNode panelsNode;

        if (dashboardNode.path("dashboard").path("panels").isEmpty()) {
            panelsNode = dashboardNode.path("dashboard").path("rows").get(0).path("panels");
        } else {
            panelsNode = dashboardNode.path("dashboard").path("panels");
        }

        ObjectNode panelNode = null;
        for (JsonNode panel : panelsNode) {
            if (panel.get("id").asInt() == panelId) {
                panelNode = (ObjectNode) panel;
                break;
            }
        }

        if (panelNode == null) {
            throw new Exception("Panel with ID " + panelId + " not found in dashboard " + dashboardTitle);
        }

        // Check if fieldConfig exists in panelNode
        if (!panelNode.has("fieldConfig")) {
            ObjectNode fieldConfigNode = objectMapper.createObjectNode();
            panelNode.set("fieldConfig", fieldConfigNode);
        }

        // Check if defaults exists in fieldConfigNode
        ObjectNode fieldConfigNode = (ObjectNode) panelNode.path("fieldConfig");
        if (!fieldConfigNode.has("defaults")) {
            ObjectNode defaultsNode = objectMapper.createObjectNode();
            fieldConfigNode.set("defaults", defaultsNode);
        }

        ObjectNode defaultsNode = (ObjectNode) fieldConfigNode.path("defaults");

        // Check if mappings exists in defaultsNode
        if (!defaultsNode.has("mappings")) {
            ArrayNode mappingsNode = objectMapper.createArrayNode();
            defaultsNode.set("mappings", mappingsNode);
        }

        ArrayNode mappingsNode = (ArrayNode) defaultsNode.path("mappings");
        boolean found = false;
        for (JsonNode mapping : mappingsNode) {
            if (mapping.path("type").asText().equals("value")) {
                ((ObjectNode) mapping.path("options").path("0"))
                        .put("text", "DOWN")
                        .put("color", "dark-red")
                        .put("index", 1);
                ((ObjectNode) mapping.path("options").path("1"))
                        .put("text", "UP")
                        .put("color", "green")
                        .put("index", 0);

                found = true;
                break;
            }
        }

        if (!found) {
            ObjectNode valueMapping = objectMapper.createObjectNode();
            valueMapping.put("type", "value");
            ObjectNode options = objectMapper.createObjectNode();
            options.put("0", objectMapper.createObjectNode()
                    .put("text", "DOWN")
                    .put("color", "dark-red")
                    .put("index", 1));
            options.put("1", objectMapper.createObjectNode()
                    .put("text", "UP")
                    .put("color", "green")
                    .put("index", 0));
            valueMapping.set("options", options);
            mappingsNode.add(valueMapping);
        }

        // Send the updated dashboard JSON to the server
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db", HttpMethod.POST, updateRequestEntity, String.class);

        // Check if the update was successful
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard updated successfully");
        } else {
            throw new RuntimeException("Dashboard update failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
        }
    }
    public void setPercentUnit(String dashboardTitle, Integer panelId) throws Exception {
        String dashboardJson = grafanaClient.GetDashboard(dashboardTitle);
        System.out.println(dashboardJson);

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode dashboardNode = (ObjectNode) objectMapper.readTree(dashboardJson);
        JsonNode panelsNode;

        if (dashboardNode.path("dashboard").path("panels").isEmpty()) {
            System.out.println("lena1");
            panelsNode = dashboardNode.path("dashboard").path("rows").get(0).path("panels");
        } else {
            System.out.println("lena2");
            panelsNode = dashboardNode.path("dashboard").path("panels");
        }

        ObjectNode panelNode = null;
        for (JsonNode panel : panelsNode) {
            System.out.println("panel" + panel);
            if (panel.get("id").asInt() == panelId) {
                panelNode = (ObjectNode) panel;
                System.out.println(panelNode); // OK
                break;
            }
        }

        if (panelNode == null) {
            throw new Exception("Panel with ID " + panelId + " not found in dashboard " + dashboardTitle);
        }

        // Check if fieldConfig exists in panelNode
        if (!panelNode.has("fieldConfig")) {
            ObjectNode fieldConfigNode = objectMapper.createObjectNode();
            panelNode.set("fieldConfig", fieldConfigNode);
        }

        // Check if defaults exists in fieldConfigNode
        ObjectNode fieldConfigNode = (ObjectNode) panelNode.path("fieldConfig");
        if (!fieldConfigNode.has("defaults")) {
            ObjectNode defaultsNode = objectMapper.createObjectNode();
            fieldConfigNode.set("defaults", defaultsNode);
        }

        ObjectNode defaultsNode = (ObjectNode) fieldConfigNode.path("defaults");
        defaultsNode.put("unit", "percent");
        defaultsNode.put("max", 100);
        defaultsNode.put("min", 0);

        System.out.println("dashboardNode" + dashboardNode);

        RestTemplate restTemplate = new RestTemplate();

        // Send the updated dashboard JSON to the server
        HttpHeaders updateHeaders = new HttpHeaders();
        updateHeaders.setContentType(MediaType.APPLICATION_JSON);
        updateHeaders.set("Authorization", "Bearer " + apiKey);
        HttpEntity<String> updateRequestEntity = new HttpEntity<>(objectMapper.writeValueAsString(dashboardNode), updateHeaders);
        System.out.println(dashboardNode);
        ResponseEntity<String> updateResponse = restTemplate.exchange(grafanaUrl + "api/dashboards/db", HttpMethod.POST, updateRequestEntity, String.class);

        // Check if the update was successful
        if (updateResponse.getStatusCode().is2xxSuccessful()) {
            System.out.println("Dashboard updated successfully");
        } else {
            throw new RuntimeException("Dashboard update failed: " + updateResponse.getStatusCodeValue() + " " + updateResponse.getBody());
        }
    }


    public String viewSettings(String indice){
        if(indice=="uptime"){

        }
        return "";
    }
}
