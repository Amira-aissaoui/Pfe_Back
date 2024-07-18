package com.expo.grafana.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.expo.prometheus.service.PrometheusQuery;

@Service
public class OverViewPanelsService {

    private final PrometheusQuery prometheusQuery;
    private final GrafanaClient grafanaClient;
    private final PanelClient panelClient;

    @Autowired
    public OverViewPanelsService(PrometheusQuery prometheusQuery, GrafanaClient grafanaClient, PanelClient panelClient) {
        this.prometheusQuery = prometheusQuery;
        this.grafanaClient = grafanaClient;
        this.panelClient = panelClient;
    }

    public void addPanel(String dashboardTitle, String ip, String port, String appType) throws Exception {
        String instance = ip + ":" + port;
        String deploymentWhere = prometheusQuery.getDeploymentWhere(instance);
       // String deploymentWhere="VM";
      //  String deploymentWhere="K8s";

        Map<String, String> expressions;
        ArrayList<String> chartTypes;

        if (deploymentWhere.equals("VM")) {
            if (appType.contains("Monolithic")) {
                expressions = getExpressionsMonolithicVM(instance);
                chartTypes = getChartMonolithicVM();
            } else if (appType.contains("Microservice")) {
                expressions = getExpressionsMsVM(instance);
                chartTypes = getChartMsVM();
            } else {
                throw new IllegalArgumentException("Invalid app type");
            }
        } else {
            if (appType.contains("Monolithic")) {
                expressions = getExpressionsMonolithicK8s(instance);
                chartTypes = getChartMonolithicK8s();
                System.out.println("hereeeeeeee");
            } else if (appType.contains("Microservice")) {
                expressions = getExpressionsMsK8s(instance);
                chartTypes = getChartMsK8s();
            } else {
                throw new IllegalArgumentException("Invalid app type");
            }
        }

        //List<String> panels = grafanaClient.getAllPanelIds(dashboardTitle);
        //if (!panels.isEmpty()) {
          //  Collections.sort(panels);
            int id =  1;

            int chartIndex = 0;
            for (Map.Entry<String, String> entry : expressions.entrySet()) {
                System.out.println("entry"+entry);
                System.out.println("expressions.entrySet()"+expressions.entrySet());

                String metricName = entry.getKey();
                String metricExpression = entry.getValue();
                String chartType = chartTypes.get(chartIndex);

                panelClient.addPanel(dashboardTitle, metricName, metricExpression, chartType, id, 0);
                System.out.println("added");
                if(metricExpression.contains("jvm_buffer_memory_used_bytes")){
                    panelClient.setFormat(dashboardTitle,id,"decbytes");


                }
                if ((metricExpression.contains("avg") && !metricExpression.contains("jvm_buffer_memory_used_bytes") ) || metricExpression.contains("rate") || metricExpression.contains("ratio")
                        || metricExpression.contains("node_memory_SwapTotal_bytes")
                        || metricExpression.contains("node_memory_MemTotal_bytes")
                        || metricExpression.contains("system_cpu_usage")
                        || metricExpression.contains("node_memory_MemAvailable_bytes")
                        || metricExpression.contains("node_filesystem_avail_bytes")){
                    panelClient.setPercentUnit(dashboardTitle,id);
                }
                else if(metricExpression.contains("time") || metricExpression.contains("seconds")){
                        panelClient.setFormat(dashboardTitle,id,"s");
                        System.out.println("ok mrigl");
                }
                    chartIndex++;

                id++;

        }
    }

    private Map<String, String> getExpressionsMonolithicVM(String instance) {
        Map<String, String> expressions = new HashMap<>();
        expressions.put("CPU Usage Over Time", String.format("sum(system_cpu_usage{instance=\"%s\"})", instance));
        expressions.put("Buffer Memory Usage Over Time", String.format("avg(jvm_buffer_memory_used_bytes{instance=\"%s\"})", instance));
        expressions.put("Network Traffic", String.format("sum(http_server_requests_seconds_count{instance=\"%s\"})", instance));
      //  expressions.put("Disk Usage Distribution", String.format("avg(cache_size{instance=\"%s\"})", instance));
        return expressions;
    }

    private ArrayList<String> getChartMonolithicVM() {
        return new ArrayList<>(Arrays.asList("gauge", "timeseries", "stat"));
    }

    private Map<String, String> getExpressionsMsVM(String instance) {
        Map<String, String> expressions = new HashMap<>();
        expressions.put("CPU Usage Over Time", String.format("sum(system_cpu_usage{instance=\"%s\"})*100 ", instance));
        expressions.put("Memory Usage Over Time", String.format("avg(jvm_buffer_memory_used_bytes{instance=\"%s\"})*100 ", instance));
        expressions.put("Network Traffic", String.format("sum(http_server_requests_seconds_count{instance=\"%s\"}) ", instance));
      //  expressions.put("Disk Usage Distribution", String.format("avg(cache_size{instance=\"%s\"})", instance));
        expressions.put("HTTP Requests", String.format("sum(http_server_requests_seconds_sum{instance=\"%s\"})", instance));
        return expressions;
    }

    private ArrayList<String> getChartMsVM() {
        return new ArrayList<>(Arrays.asList("gauge", "gauge", "barchart", "histogram"));
    }

    private Map<String, String> getExpressionsMonolithicK8s(String instance) {
        Map<String, String> expressions = new HashMap<>();
      //  expressions.put("CPU Usage Over Time", String.format("sum(kube_pod_container_resource_requests_cpu_cores{namespace=\"%s\"})", instance));
       // expressions.put("Memory Usage Over Time", String.format("sum(kube_pod_container_resource_requests_memory_bytes{namespace=\"%s\"})", instance));
        //expressions.put("Network Traffic", String.format("sum(kube_pod_container_network_transmit_bytes_total{namespace=\"%s\"})", instance));
        //expressions.put("Disk Usage Distribution", String.format("sum(node_filesystem_size{nodename=\"%s\"}) - sum(node_filesystem_free{nodename=\"%s\"})", instance, instance));
        //expressions.put("HTTP Requests", String.format("http_requests_total{instance=\"%s\"}", instance));
        expressions.put("UP Time", String.format("node_time_seconds{instance=\"%s\"} - node_boot_time_seconds{instance=\"%s\"}", instance,instance));
        expressions.put("RAM Used", String.format("100 - ((node_memory_MemAvailable_bytes{instance=\"%s\"} * 100) / node_memory_MemTotal_bytes{instance=\"%s\"})", instance,instance));
        expressions.put("Busy state of all CPU cores together (5 min average)", String.format("avg(node_load5{instance=\"%s\"}) /  count(count(node_cpu_seconds_total{instance=\"%s\"}) by (cpu)) * 100",instance,instance));
        expressions.put("CPU Busy", String.format( "(sum by(instance) (irate(node_cpu_seconds_total{instance=\"%s\", mode!=\"idle\"}[$__rate_interval])) / on(instance) group_left sum by (instance)((irate(node_cpu_seconds_total{instance=\"%s\"}[$__rate_interval])))) * 100",instance,instance));

        return expressions;
    }

    private ArrayList<String> getChartMonolithicK8s() {
        return new ArrayList<>(Arrays.asList("gauge", "gauge", "gauge", "gauge", "gauge"));
    }

    private Map<String, String> getExpressionsMsK8s(String instance) {
        Map<String, String> expressions = new HashMap<>();
        //expressions.put("CPU Usage Over Time", String.format("sum(http_requests_total{deployment=\"%s\"})", instance));
       // expressions.put("Memory Usage Over Time", String.format("avg(container_memory_usage_bytes{deployment=\"%s\"})", instance));
       // expressions.put("Network Traffic", String.format("sum(http_server_requests_seconds_count{deployment=\"%s\"})", instance));
      //  expressions.put("Disk Usage Distribution", String.format("sum(node_filesystem_size{nodename=~\"%s\"}) - sum(node_filesystem_free{nodename=~\"%s\"})", instance, instance));
      //  expressions.put("HTTP Requests", String.format("sum(rate(http_requests_total{instance=\"%s\"}[1m]))", instance));
        expressions.put("UP Time", String.format("node_time_seconds{instance=\"%s\"} - node_boot_time_seconds{instance=\"%s\"}", instance,instance));
        expressions.put("RAM Used", String.format("100 - ((node_memory_MemAvailable_bytes{instance=\"%s\"} * 100) / node_memory_MemTotal_bytes{instance=\"%s\"})", instance,instance));
        expressions.put("Busy state of all CPU cores together (5 min average)", String.format("avg(node_load5{instance=\"%s\"}) /  count(count(node_cpu_seconds_total{instance=\"%s\"}) by (cpu)) * 100",instance,instance));
        expressions.put("CPU Busy", String.format( "(sum by(instance) (irate(node_cpu_seconds_total{instance=\"%s\", mode!=\"idle\"}[$__rate_interval])) / on(instance) group_left sum by (instance)((irate(node_cpu_seconds_total{instance=\"%s\"}[$__rate_interval])))) * 100",instance,instance));

        return expressions;
    }

    private ArrayList<String> getChartMsK8s() {
        return new ArrayList<>(Arrays.asList("gauge", "gauge", "gauge", "gauge", "gauge"));
    }
}

