package com.expo.prometheus.service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.expo.healthcheck.service.AppHealthCheckService;

@Service
public class PrometheusConfigFileGenerator {


    @Value("${prometheus.config.path}")
    private String prometheusConfigPath;
    @Value("${alertmanager.config.path}")
    private String alertManagerConfigPath;

    private static final String ALERT_FILE_NAME = "alert.rules.yml";
    @Value("${privateKeyPath2}")
    private  String RESOURCES_DIRECTORY;

    private static final String PROMETHEUS_CONFIG_FILE="prometheus.yml";

    String theLocalFile=prometheusConfigPath+PROMETHEUS_CONFIG_FILE;
    @Value("${alertmanager.restart.command}")
    private String alertManagerRestartCommand;
    @Value("${prometheus.restart.command}")
    private String prometheusRestartCommand;

    @Value("${prometheus.server.url}")
    private String prometheusServerurl;
    @Value("${sonarqube.path}")
    private String sonarqube_metric_path;
    @Value("${sonarqube.url}")
    private String sonarqube_url;
    @Value("${global.metric.url}")
    private String global_url;

    public PrometheusConfigFileGenerator(PrometheusAlertService prometheusAlertService, AppHealthCheckService reloadPrometheus) {
        this.prometheusAlertService = prometheusAlertService;
        this.reloadPrometheus = reloadPrometheus;
    }

    private PrometheusAlertService prometheusAlertService;
    private final AppHealthCheckService reloadPrometheus;





    public void generateConfigFile() {
        // Define the alertmanager configuration content
        String configContent = "global:\n" +
                "  scrape_interval: 15s\n" +
                "rule_files:\n" +
                "  - "+ALERT_FILE_NAME+"\n" +
                "alerting:\n" +
                "  alertmanagers:\n" +
                "    - static_configs:\n"+
                "      - targets: ['localhost:9093']\n" +
                "scrape_configs:\n" +
                "  - job_name: SonarQube'\n" +
                "    metrics_path: "+ sonarqube_metric_path +"\n" +
                "    static_configs :\n" +
                "    - targets: \n" +
                "      - '"+sonarqube_url+"'\n" ;



        // Specify the path and name for the configuration file
        String filePath = prometheusConfigPath+"/"+PROMETHEUS_CONFIG_FILE;

        try {
            FileWriter writer = new FileWriter(filePath);
            writer.write(configContent);
            writer.close();
            System.out.println("Prometheus configuration file generated successfully.");
            this.reloadPrometheus.reloadPrometheusAlertManagerServer(prometheusServerurl);
        } catch (IOException e) {
            System.err.println("Failed to write the configuration file: " + e.getMessage());
        }
    }

    public void addAppToFile(String jobName, String metricsPath, String ipaddr, String port) {
        if (ipaddr.isEmpty() || port.isEmpty() || jobName.isEmpty()) {
            System.err.println("IP address, port, and jobName cannot be empty.");
            return;
        }
        jobName=jobName+"instance:"+ipaddr+":"+"port"+port;

        String receiver = generateAppConfig(jobName, metricsPath, ipaddr, port);
        try {
            Path prometheusFilePath = Path.of(prometheusConfigPath+"/", PROMETHEUS_CONFIG_FILE);

            if (Files.exists(prometheusFilePath)) {
                List<String> lines = Files.readAllLines(prometheusFilePath);
                boolean instanceExists = false;
                boolean jobNameExists = false;

                // Check if the instance and jobName already exist in the configuration file
                for (String line : lines) {
                    if (line.contains(ipaddr + ":" + port)) {
                        instanceExists = true;
                    }
                    if (line.contains("job_name: " + jobName)) {
                        jobNameExists = true;
                    }
                }

                if (instanceExists) {
                    System.out.println("Instance already exists in the file. Skipping addition.");
                } else {
                    if (jobNameExists) {
                        System.out.println("JobName already exists in the file. Please choose a unique jobName.");
                    } else {
                        Files.writeString(prometheusFilePath, receiver + "\n", StandardOpenOption.APPEND);
                        System.out.println("Instance added to the file successfully.");
                        this.reloadPrometheus.reloadPrometheusAlertManagerServer(prometheusServerurl);
                    }
                }
            } else {
                System.err.println("Rule File generated");
                generateConfigFile();
                Files.writeString(prometheusFilePath, receiver + "\n", StandardOpenOption.APPEND);
                System.out.println("Instance added to the file successfully.");
                this.reloadPrometheus.reloadPrometheusAlertManagerServer(prometheusServerurl);
            }
        } catch (IOException e) {
            System.err.println("Failed to add the instance to the file: " + e.getMessage());
        }
    }


  /*  public void addAppToFile(String jobName, String metricsPath, String ipaddr, String port ) {
        String receiver=generateAppConfig(jobName,metricsPath,ipaddr,port);
        try {
            Path prometheusFilePath = Path.of("/shared/", PROMETHEUS_CONFIG_FILE);

            if (Files.exists(prometheusFilePath)) {
                Files.writeString(prometheusFilePath, receiver + "\n", StandardOpenOption.APPEND);
                System.out.println("Target added to the file successfully.");
                this.reloadPrometheus.reloadPrometheusAlertManagerServer(prometheusServerurl);

            }
            else {
                System.err.println("Rule File generated");

                generateConfigFile();
                Files.writeString(prometheusFilePath, receiver + "\n", StandardOpenOption.APPEND);
                this.reloadPrometheus.reloadPrometheusAlertManagerServer(prometheusServerurl);

            }
        } catch (IOException e) {
            System.err.println("Failed to add the rule to the file: " + e.getMessage());
        }
    }*/
    public static String generateAppConfig(String jobName, String metricsPath, String ipaddr, String port ) {
    if(port.equals("9100") || port.equals("30462")){
        metricsPath="/metrics";
    }
    if(port.equals("8088")){
        metricsPath="/metrics/prometheus";

    }
        String receiverConfig =
                "  - job_name: '" + jobName + "'\n" +
                        "    metrics_path: " + metricsPath + "\n" +
                        "    static_configs :\n" +
                        "    - targets: \n" +
                        "      - '" + ipaddr +":"+port + "'\n" ;

        return receiverConfig;

    }



}
