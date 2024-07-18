package com.expo.prometheus.controller;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expo.healthcheck.service.AppHealthCheckService;
import com.expo.prometheus.model.AlertInfo;
import com.expo.prometheus.model.RuleInfo;
import com.expo.prometheus.service.PrometheusAlertService;
import com.expo.prometheus.service.RuleFileGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
@RequestMapping("/api/prometheus/rulefile")
@CrossOrigin(origins = "http://localhost:4200/")
public class RuleFileController {
    private final RuleFileGenerator ruleFileGenerator;
    private final PrometheusAlertService prometheusService;
    private static final String RULES_FILE_NAME = "alert.rules.yml";
    //private static final String RESOURCES_DIRECTORY = "src/main/resources/";
    @Value("${privateKeyPath2}")
    private  String RESOURCES_DIRECTORY;
//    private String theLocalRulesFile=RESOURCES_DIRECTORY+RULES_FILE_NAME;
    private String theLocalRulesFile=RULES_FILE_NAME;

    @Value("${prometheus.restart.command}")
    private String prometheusRestartCommand;
    private final AppHealthCheckService reloadPrometheus;
    @Value("${prometheus.server.url}")
    private String prometheusurl;


    @Autowired
    public RuleFileController(RuleFileGenerator ruleFileGenerator, PrometheusAlertService prometheusService, AppHealthCheckService reloadPrometheus) {
        this.ruleFileGenerator = ruleFileGenerator;
        this.prometheusService = prometheusService;
        this.reloadPrometheus = reloadPrometheus;
    }

        @GetMapping("/generate-rule-file")
    public String generateRuleFile() throws IOException {
        ruleFileGenerator.generateRuleFile();
        return "Rule file generated successfully.";
    }

    @PostMapping("/add-rule")
    public void addRuleToFile(@RequestParam(value="alertname")String alertname,@RequestParam(value = "instance")List <String> instance,@RequestParam(value = "metric")String metric,
    @RequestParam(value="severity")String severity,@RequestParam(value="comparaison")String comparaison,
       @RequestParam(value="value")String value,@RequestParam(value="time")String time,
    @RequestParam(value = "summary")String summary,
     @RequestParam(value = "description")String description
    )
            throws IOException {
    System.out.println("instalce"+instance);
        ruleFileGenerator.addRuleToFile(alertname,instance,metric,severity,comparaison,value,time,summary,description);
        this.reloadPrometheus.reloadPrometheusAlertManagerServer(prometheusurl);

    }

    @PostMapping(value = "/modify_rules_instance")
    public ResponseEntity<Boolean> modifyrulesintance(@RequestParam(value = "oldinstance")String oldinstance,@RequestParam(value = "newinstance")String newinstance) throws IOException {

        boolean ismodified=this.ruleFileGenerator.modifyInstanceRules(oldinstance, newinstance);
        if(ismodified){

            this.reloadPrometheus.reloadPrometheusAlertManagerServer(prometheusurl);
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.status(400).body(false);

    }


    @PostMapping(value = "/modify_rule")
    public ResponseEntity<Boolean> modifyRule(@RequestBody List<Object>  modifiedValues) throws IOException {
          System.out.println("mod: " + modifiedValues);
       List rulename=  ((List)(modifiedValues.get(0)));
       String rule=rulename.get(1).toString();
        List instancelist=  ((List)(modifiedValues.get(1)));
        String instance=instancelist.get(1).toString();
        System.out.println("rulename: " +  rule);
        System.out.println("instance: " +  instance);

       for (int i = 2; i < modifiedValues.size(); i++) {
            List values = (List) modifiedValues.get(i);
            String property=values.get(0).toString();
            String newvalue=values.get(1).toString();
            System.out.println("property"+property);
            System.out.println("property2"+newvalue );

            boolean ruleModified = ruleFileGenerator.modifyRule(rule, property,newvalue, instance);
            if (!ruleModified) {
                return ResponseEntity.status(400).body(false);


            }
        }

        this.reloadPrometheus.reloadPrometheusAlertManagerServer(prometheusurl);
        return ResponseEntity.ok(true);


    }

    @GetMapping("/get_rules")
    public ResponseEntity<List<RuleInfo>> getRules(@RequestParam(value = "instances") List<String> instances) throws JsonProcessingException {
        List<RuleInfo> rules = new ArrayList<>();
        System.out.println("instances"+instances);

        for (String instance : instances) {
            System.out.println("instance"+instance);
            List<RuleInfo> instanceRules = ruleFileGenerator.getRulesByInstance(instance);
            System.out.println("instanceRules"+instanceRules);
            rules.addAll(instanceRules);
            System.out.println("rules"+rules);

        }

        System.out.println("rules: " + rules);

        if (!rules.isEmpty()) {
            return ResponseEntity.ok(rules);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // NEIN
    @PostMapping("/delete_alert")
    public ResponseEntity<Boolean> deleteAlertRule(@RequestParam(value = "rulename") String rulename) throws JsonProcessingException {
        boolean ruleDeleted = ruleFileGenerator.deleteAlertRule(rulename);

        if (ruleDeleted) {
            this.reloadPrometheus.reloadPrometheusAlertManagerServer(prometheusurl);
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(404).body(false);
        }
    }
    @PostMapping("/delete_rule")
    public ResponseEntity<Boolean> deleteRule(@RequestParam(value = "rulename") String rulename,@RequestParam(value="instance")String instance) throws JsonProcessingException {
        boolean ruleDeleted = ruleFileGenerator.deleteRule(rulename,instance);

        if (ruleDeleted) {
            this.reloadPrometheus.reloadPrometheusAlertManagerServer(prometheusurl);

            return ResponseEntity.ok(true);

        } else {
            return ResponseEntity.status(404).body(false);
        }
    }

    @GetMapping("/get_alert_status")
    public List<AlertInfo> getAlerts(@RequestParam("instances") List<String> instances) {
        List<AlertInfo> alertsMap = new ArrayList<>();

        for (String instance : instances) {
            List<AlertInfo> alerts = ruleFileGenerator.getAlertStatus(instance);

            if (alerts != null && !alerts.isEmpty()) {
                alertsMap.addAll(alerts);
            }
        }

        System.out.println("alertsMap"+alertsMap);
        return alertsMap;
    }



}
