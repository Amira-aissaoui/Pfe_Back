package com.expo.grafana.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expo.grafana.controller.DashboardController.DashboardCalendarRequest;
import com.expo.grafana.service.DashboardBuilder;
import com.expo.grafana.service.GrafanaClient;
import com.expo.grafana.service.MonitoringService;
import com.expo.grafana.service.OverViewPanelsService;
import com.expo.grafana.service.PanelClient;
import com.expo.prometheus.service.PrometheusQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.v3.oas.annotations.parameters.RequestBody;




//kiff kiff à verifier le path du panels here
// f actia temchili .get("dashboard").get(panels) f pc mteei .get("rows").get(0).get("panels") c donc à verifier aalech (tested 06 Mai 2023)
//ps il y'avait un changement de la version du grafana que j'utilise donc peut etre l json à générer tbadlet l format mteeo

//@RestController

@RequestMapping("/api/grafana")
@RestController
@CrossOrigin(origins = "http://localhost:4200/")
public class DashboardController {

    @Autowired
    private DashboardBuilder dashboardBuilder;

    @Autowired
    private GrafanaClient grafanaClient;
    @Autowired
    private PanelClient panelClient;
    @Autowired
    private PrometheusQuery prometheusQuery;

    @Autowired
    private MonitoringService monitoringService;
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);






    @Autowired
    private final OverViewPanelsService overViewPanelsService;
    public DashboardController(OverViewPanelsService overViewPanelsService) {
        this.overViewPanelsService = overViewPanelsService;
    }

    /* @GetMapping("/checkDeployment")
    public Mono<String> checkDeployment(@RequestParam String ip, @RequestParam String port) {
        String url = "http://localhost:8080/api/prometheus/query/check_deployment?ip=" + ip + "&port=" + port;
        return monitoringService.get()
                .uri(url)
                .retrieve()
                .bodyToMono(DeploymentResponse.class)
                .map(response -> "Deployment: " + response.getDeployment());
    }*/



    
    static class DeploymentResponse {
        private String Deployment;
        public String getDeployment() {
            return Deployment;
        }
        public void setDeployment(String deployment) {
            Deployment = deployment;
        }
    }
    @PostMapping("/import-dashboard")
    public String importDashboard(@RequestParam String dashboardTitle) throws JsonProcessingException {
        monitoringService.importDashboard(dashboardTitle);
        return "Dashboard imported successfully.";
    }


    @PostMapping("/dashboard")
    public ResponseEntity<?> createDashboard(@RequestParam(value = "title") String projectName,
                                            @RequestParam(value = "targets") String[] targets) throws JsonProcessingException {
        // Using the projectName as the title
        String title = projectName;

        if (grafanaClient.doesDashboardExist(title)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Dashboard already exists");
        }

        String jsonPayload = dashboardBuilder.buildDashboard(title, targets);
        grafanaClient.createDashboard(jsonPayload);
      //  System.out.println(jsonPayload);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    Integer id=6;


    @PostMapping("/panel")
    public void addPanel(@RequestParam(value = "dashboardTitle") String dashboardTitle,@RequestParam(value = "PanelTitle") String PanelTitle,
                    @RequestParam(value = "target") String targetExpr,@RequestParam (value= "panelChart")String chart,
                    @RequestParam(value="ip")String ip,
                    @RequestParam(value="port")String port,@RequestParam(value = "tag")String tag) throws Exception {

        Random random = new Random();
        int testid = random.nextInt();
        String instance;
        String target;

        //fl cas aandi sonar metric
        if (port == null || port.isEmpty() || port.equalsIgnoreCase("none")) {
            instance = ip;
           // System.out.println("over here");
        }
        else{
            instance=ip+":"+port;

        }

        if(!grafanaClient.getAllPanelIds(dashboardTitle).isEmpty()){
            List<String> panels = grafanaClient.getAllPanelIds(dashboardTitle);
            Collections.sort(panels);
            if((! panels.contains(Integer.toString(testid)))){
                id=testid;
            }
            else{
                testid = random.nextInt();
                id=testid;
            }
            }



        if (targetExpr.equals("")) {
            instance="\""+ip+":"+port+"\"";

            target = PanelTitle.replace(" ", "_");
            target = target.toLowerCase();

            target+="{";
            target += String.format("instance=%s",instance);
            target+="}";
            System.out.println(target);

            if (target.contains("process_start_time_seconds")) {

                target=target+"*1000";


            }
            if (target.contains("process_cpu_seconds_total")) {
                target=target+"*100";


            }
            if (target.contains("total")) {
                target="sum("+target+")";


            }
            if(target.contains("logback_events_total")){
                    target="increase("+target+"[1m])";
                    chart="timeseries";
            }
        }
        else{
            target=this.prometheusQuery.getQueryExpression(targetExpr,ip,port);

        }

        int tagnumber = Integer.parseInt(tag);

        panelClient.addPanel(dashboardTitle,PanelTitle, target, chart,id ,tagnumber);
        if (target.contains("process_start_time_seconds")) {
            panelClient.setFormat(dashboardTitle,id,"dateTimeAsIso");

        }


        else if((target.contains("time") || target.contains("seconds")) && !target.equals("node_cpu_seconds_total")){
            panelClient.setFormat(dashboardTitle,id,"s");
        }

        else if(target.contains("celsius") ){
            panelClient.setFormat(dashboardTitle,id,"celsius");
        }
        else if (target.contains("bytes")) {
            panelClient.setFormat(dashboardTitle,id,"decbytes");

        }
        else if (target.contains("healthcheck") || target.equals("up")){
            panelClient.setPanelText(dashboardTitle,id);
        }
        else if (target.contains("avg") || target.contains("rate") || target.contains("ratio")
                || target.contains("node_memory_SwapTotal_bytes")
                || target.contains("node_memory_MemTotal_bytes")
                || target.contains("system_cpu_usage")
                || target.contains("node_memory_MemAvailable_bytes")
                || target.contains("node_filesystem_avail_bytes")){
            panelClient.setPercentUnit(dashboardTitle,id);
        }


    }


    /*@GetMapping("setunit")
    public void setUnit(@RequestParam(value="dashboardTitle")String title,@RequestParam(value = "id")Integer id) throws Exception {
        panelClient.setFormat(title,id);

    }*/
    @PostMapping("/deletePanel")
    public void deletePanel(@RequestParam (value="dashboardTitle") String dashboardTitle, @RequestParam (value="PanelTitle")String panelTitle) throws JsonProcessingException{

        panelClient.deletePanel(dashboardTitle, panelTitle);

    }
    @DeleteMapping("/delete-Panel")
    public ResponseEntity<?> deletePanelById(@RequestParam (value="dashboardTitle") String dashboardTitle, @RequestParam (value="PanelId")String panelId) throws JsonProcessingException{

        if (dashboardTitle =="" || panelId=="") {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        }


        panelClient.deletePanelById(dashboardTitle, panelId);


        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @PostMapping("/deleteDashboard")
    public void deleteDashboard(@RequestParam (value="dashboardTitle") String dashboardTitle) throws JsonProcessingException{

        grafanaClient.deleteDashboard(dashboardTitle);

    }
    @PostMapping("/modifyDashboard")
    public ResponseEntity<String> modifyDashboard(
            @RequestParam(value = "dashboardTitle") String dashboardTitle,
            @RequestParam(value = "newTitle", required = false) String newTitle,
            @RequestParam(value = "refresh", required = false) String refresh,
            @RequestParam(value = "timeFrom", required = false) String timeFrom,
            @RequestParam(value = "timeTo", required = false) String timeTo,
            @RequestParam(value = "timeRange", required = false) String timeRange) {
        try {
         //   String dashboardUid=grafanaClient.getDashboardUidByTitle(dashboardTitle);
            grafanaClient.modifyDashboard(dashboardTitle,newTitle,refresh,timeFrom,timeTo,timeRange);
            return ResponseEntity.ok("Dashboard updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating dashboard: " + e.getMessage());
        }
    }

    @PostMapping("/getpanels")
        public List<JsonNode> getPanels(@RequestParam (value="dashboardTitle") String dashboardTitle) throws JsonProcessingException {

        return  grafanaClient.GetPanels(dashboardTitle);

        }

    @PostMapping("/modify_panel_instance")
    public void modifypanelinstance(@RequestParam (value="dashboardTitle")String dashboardTitle,@RequestParam (value="panelId") int panelId,@RequestParam(value = "newinstance")String newInstance) throws Exception {
        panelClient.modifyPanelInstance(dashboardTitle,panelId,newInstance);
    }

    @PostMapping("/modify_all_panel_instance")
    public void modifyallpanelsinstances(@RequestParam (value="dashboardTitle")String dashboardTitle,@RequestParam(value = "tag")String tag,@RequestParam(value = "newinstance")String newInstance) throws Exception {
        List<String> panels = grafanaClient.getAllPanelIdsByTag(dashboardTitle,tag);
        if(!panels.isEmpty()){
            for(String panelID:panels){
                panelClient.modifyPanelInstance(dashboardTitle, Integer.parseInt(panelID),newInstance);

            }
        }


    }




    

    //-----------------------add exper
        @PostMapping("/modifypanel")
        public void modifyPanel(@RequestParam (value="dashboardTitle")String dashboardTitle,@RequestParam (value="panelId") int panelId,@RequestParam (value="newExper",required = false) String newExper,  @RequestParam (value="newTitle" ,required = false)String newTitle ,@RequestParam (value="newType",required = false)String newType) throws Exception {
        System.out.println("newType"+newType);
            System.out.println("newType"+newTitle);
            System.out.println("newExper"+newExper);



            panelClient.modifyPanel(dashboardTitle, panelId,newTitle,newType, newExper);
    }





    @PostMapping("/getpanelbyid")
    public JsonNode getPanelById(@RequestParam (value="panelid") String panelId,@RequestParam(value = "dashboardTitle") String dashboardTitle) throws JsonProcessingException {


        return panelClient.getPanelById(panelId,dashboardTitle);
    }
    @PostMapping("/getpanelbytitle")
    public JsonNode getPanelByTitle(@RequestParam (value="dashboardTitle") String dashboardTitle,@RequestParam(value = "panelTitle") String panelTitle) throws JsonProcessingException {
        return  panelClient.getPanelByTitle(dashboardTitle,panelTitle);

    }
    @PostMapping("/getPanelIdByTitle")
        public String getPanelIdByTitle(@RequestParam(value="dashboardTitle") String dashboardTitle , @RequestParam(value = "panelTitle") String panelTitle) throws IOException {
        return panelClient.getPanelIdByTitle(dashboardTitle,panelTitle);
    }
    @PostMapping("/getDashboard")
    public JsonNode findDashbordByTitle(@RequestParam (value="dashboardTitle")String dashboardTitle) throws JsonProcessingException {
return  grafanaClient.getDashboardByTitle(dashboardTitle);
    }
    @GetMapping("/get-template-uid")
    public JsonNode getTemplateUidByTitle() throws IOException {
    String Uid= grafanaClient.getDashboardUidByTitle("template");
        ObjectMapper objectMapper=new ObjectMapper();
        ObjectNode rootNode=objectMapper.createObjectNode();
        rootNode.put("uid",Uid);
        JsonNode jsonNode=rootNode;
        return jsonNode;

    }

    @PostMapping("/dashboard-uid")
    public ResponseEntity<JsonNode> getDashboardUid(@RequestParam("dashboardTitle") String dashboardTitle) throws IOException {
        String uid = grafanaClient.getDashboardUidByTitle(dashboardTitle);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        if (uid != null) {
            rootNode.put("uid", uid);
        }
        JsonNode jsonNode = rootNode;
        System.out.println(jsonNode);
        return ResponseEntity.ok(jsonNode);
    }


    @GetMapping("/allpanels")
    public ResponseEntity<List<String>> getAllPanelsId(@RequestParam("dashboardTitle") String title) throws JsonProcessingException {
        List<String> panels = grafanaClient.getAllPanelIds(title);
        if (panels.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(panels);
        }
    }
    @GetMapping("/allpanelsbytag")
    public ResponseEntity<List<String>> getAllPanelsIdByTags(@RequestParam("dashboardTitle") String title,@RequestParam("tag")String tag) throws JsonProcessingException {

        List<String> panels = grafanaClient.getAllPanelIdsByTag(title,tag);
        if (panels.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(panels);
        }
    }


    

    @GetMapping("/panelexprbyid")
    public ResponseEntity<String> getPanelExprByPanelId(@RequestParam("dashboardTitle") String title, @RequestParam("panelId") String panelId) throws JsonProcessingException {
        String panelExpr = grafanaClient.getPanelExprByPanelId(title, panelId);
        if (panelExpr == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(panelExpr);
        }
    }


    @PostMapping("/generic_panel")
    public void addGenericPanel(@RequestParam(value = "dashboardTitle") String dashboardTitle,  @RequestParam(value = "ip") String ip, @RequestParam(value = "port") String port,
                                @RequestParam(value = "appType") String appType) throws Exception {

        overViewPanelsService.addPanel(dashboardTitle, ip,port, appType);
    }


//-------------------------------
@PostMapping("/calendar")
public ResponseEntity<String> updateDashboardCalendar(@RequestBody DashboardCalendarRequest request) throws JsonProcessingException {
    String dashboardTitle = request.getDashboardTitle();
    LocalDate startDate = request.getStartDate();
    LocalDate endDate = request.getEndDate();
    logger.info("Received request: dashboardTitle={}, startDate={}, endDate={}", dashboardTitle, startDate, endDate);






    try {
        // Call the dashboardCalendar method to update the dashboard
        grafanaClient.dashboardCalendar(dashboardTitle, startDate, endDate);
        // Get the updated dashboard JSON
        String updatedDashboardJson = grafanaClient.GetDashboard(dashboardTitle);
        // Return the updated dashboard JSON along with success message
        return ResponseEntity.ok("Dashboard calendar updated successfully\n" + updatedDashboardJson);
    } catch (RuntimeException e) {
        // Handle the exception appropriately
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating dashboard calendar: " + e.getMessage());
    }
}
public class DashboardCalendarRequest {
    private String dashboardTitle;
    private LocalDate startDate;
    private LocalDate endDate;


    // getters and setters
    public String getDashboardTitle() {
        return dashboardTitle;
    }
    public void setDashboardTitle(String dashboardTitle) {
        this.dashboardTitle = dashboardTitle;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
}