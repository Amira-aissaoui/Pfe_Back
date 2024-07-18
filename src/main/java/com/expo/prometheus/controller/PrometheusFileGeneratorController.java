package com.expo.prometheus.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.expo.prometheus.service.PrometheusConfigFileGenerator;

@RequestMapping("/api/prometheus/filegenerator")
@CrossOrigin(origins = "http://localhost:4200/")
@RestController
public class PrometheusFileGeneratorController {
    private final PrometheusConfigFileGenerator prometheusConfigFileGenerator;
    public PrometheusFileGeneratorController(PrometheusConfigFileGenerator prometheusConfigFileGenerator) {
        this.prometheusConfigFileGenerator = prometheusConfigFileGenerator;

    }
    @GetMapping("/generate-prometheus-config-file")
    public String generateAlertFile() throws IOException {
        prometheusConfigFileGenerator.generateConfigFile();
        return "Prometheus file generated successfully.";
    }
    @PostMapping("/add-target")
    public void addRuleToFile(@RequestParam(value="jobname")String jobName, @RequestParam(value = "metricsPath")String metricsPath,
                        @RequestParam(value="ipaddr")String ipaddr, @RequestParam(value="port")String port){

        prometheusConfigFileGenerator.addAppToFile(jobName,metricsPath,ipaddr,port);


    }
}