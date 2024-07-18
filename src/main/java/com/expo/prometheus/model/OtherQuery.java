package com.expo.prometheus.model;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherQuery {

    private Map<String, String> query;


    public Map<String, String> getQuery() {
        return query;
    }

    public OtherQuery() {
        query = getTheMap(constructTheIndice(),constructTheExpressions());

    }


    public ArrayList<String> constructTheIndice(){
        ArrayList<String> indice = new ArrayList<>(List.of(         "healthcheck",
                                                                "httprequests", //ok
                                                                "errorrates",//no
                                                                "starttime",//ok
                                                                "uptime",//ok
                                                                "status",//ok
                                                                "buildstatus",//no
                                                                "builderrors",//no
                                                                "buildtime",//no
                                                                "coverage",//no
                                                                "dup",//no
                                                                "vun",//ok
                                                                "bugs",//ok
                                                                "smells",//ok
                                                                "jdbcactive",//ok
                                                                "jdbcidle",//ok
                                                                "jdbcmin",//ok
                                                                "jdbcmax",//ok
                                                                "junit",//no
                                                                "httpavg" ,//ok
                                                                "heapmemo",
                                                                "nonheap",
                                                                "garbage",
                                                                "pausedurations",
                                                                "jvmtotal",
                                                                "cpujvm",
                                                                "cpubusy",
                                                                "sysload",
                                                                "ramused",
                                                                "swapused",
                                                                "rootfs",
                                                                "cpucores",
                                                                "ramtotal",
                                                                "swaptotal",
                                                                "diskiops",
                                                                "ioutil",
                                                                "nodescrapetime",
                                                                "tcpcnx",
                                                                "netstatinoctet",
                                                                "netstatoutoctet",
                                                                "udpout",
                                                                "udpin",
                                                                "udperrors",
                                                                "mtu",
                                                                "networkop",
                                                                "queuelen","networkspeed","arpent",
                "filenodesize",
                "filespaceavail","temp","timeslices","processrunning","kernelstack","memodynamic"




        ));
        return indice;

    }
    public ArrayList<String> constructTheExpressions(){
        ArrayList<String> expr = new ArrayList<>(List.of(
                "up",
                "http_requests_total{instance=\"%s\"}",
                "errorrates",
                "process_start_time_seconds{instance=\"%s\"}*1000",
                "process_uptime_seconds{instance=\"%s\"}",
                "up{instance=\"%s\"}", //cbon
                "buildstatus", //no
                "builderrors",//no
                "buildtime",//no
                "sonarqube_coverage{key=\"%s\"}", //no
                "dup", //no
                "sonarqube_vulnerabilities{key=\"%s\"}", //no
                "sonarqube_bugs{key=\"%s\"}", //no
                "sonarqube_code_smells{key=\"%s\"}", //no
                "jdbc_connections_active{instance=\"%s\"}",
                "jdbc_connections_idle{instance=\"%s\"}",
                "jdbc_connections_min{instance=\"%s\"}",
                "jdbc_connections_max{instance=\"%s\"}",
                "junit", //no
                "sum(irate(http_server_requests_seconds_sum{instance=\"%s\"}[1m])) / sum(irate(http_server_requests_seconds_count{instance=\"%s\"}[1m]))",
                "jvm_memory_used_bytes{instance=\"%s\"}",
                "sum(jvm_memory_used_bytes{ instance=\"%s\",area=nonheap})*100/sum(jvm_memory_max_bytes{ instance=\"%s\",area=nonheap})",
                "rate(jvm_gc_pause_seconds_count{instance=\"%s\"}[1m])",
                "rate(jvm_gc_pause_seconds_sum{instance=\"%s\"}[1m])/rate(jvm_gc_pause_seconds_count{instance=\"%s\"}[1m])",
                "sum(jvm_memory_used_bytes{instance=\"%s\"})",
                "system_cpu_usage{instance=\"%s\"}",
                // a partir mn hne l zyeda
                "(sum by(instance) (irate(node_cpu_seconds_total{instance=\"%s\", mode!=\"idle\"}[$__rate_interval])) / on(instance) group_left sum by (instance)((irate(node_cpu_seconds_total{instance=\"%s\"}[$__rate_interval])))) * 100",
                "avg(node_load5{instance=\"%s\"}) /  count(count(node_cpu_seconds_total{instance=\"%s\"}) by (cpu)) * 100",
                "100 - ((node_memory_MemAvailable_bytes{instance=\"%s\"} * 100) / node_memory_MemTotal_bytes{instance=\"%s\"})",
                "((node_memory_SwapTotal_bytes{instance=\"%s\"} - node_memory_SwapFree_bytes{instance=\"%s\"}) / (node_memory_SwapTotal_bytes{instance=\"%s\"} )) * 100",
                "100 - ((node_filesystem_avail_bytes{instance=\"%s\",mountpoint=\"/\",fstype!=\"rootfs\"} * 100) / node_filesystem_size_bytes{instance=\"%s\",mountpoint=\"/\",fstype!=\"rootfs\"})",
                "count(count(node_cpu_seconds_total{instance=\"%s\"}) by (cpu))",
                "node_memory_MemTotal_bytes{instance=\"%s\"}",
                "node_memory_SwapTotal_bytes{instance=\"%s\"}",
                "irate(node_disk_writes_completed_total{instance=\"%s\"}[$__rate_interval])",
                "irate(node_disk_io_time_seconds_total{instance=\"%s\"} [$__rate_interval])",
                "node_scrape_collector_duration_seconds{instance=\"%s\"}",
                "node_netstat_Tcp_CurrEstab{instance=\"%s\"}",
                "irate(node_netstat_IpExt_InOctets{instance=\"%s\"}[$__rate_interval])",
                "irate(node_netstat_IpExt_OutOctets{instance=\"%s\"}[$__rate_interval])",
                "irate(node_netstat_Udp_InDatagrams{instance=\"%s\"}[$__rate_interval])",
                "irate(node_netstat_Udp_OutDatagrams{instance=\"%s\"}[$__rate_interval])",
                "irate(node_netstat_Udp_InErrors{instance=\"%s\"}[$__rate_interval])",
                "node_network_mtu_bytes{instance=\"%s\"}",
                "node_network_carrier{instance=\"%s\"}",
                "node_network_transmit_queue_length{instance=\"%s\"}",
                "node_network_speed_bytes{instance=\"%s\"}",
                "node_arp_entries{instance=\"%s\"}",
                "node_filesystem_files{instance=\"%s\",device!~'rootfs'}",
                "node_filesystem_avail_bytes{instance=\"%s\",device!~'rootfs'}",
                "node_hwmon_temp_celsius{instance=\"%s\"}",
                "irate(node_schedstat_timeslices_total{instance=\"%s\"}[$__rate_interval])",
                "node_procs_running{instance=\"%s\"}",
                "node_memory_KernelStack_bytes{instance=\"%s\"}",
                "node_memory_Percpu_bytes{instance=\"%s\"}"

                ));
        return expr;

    }
    public Map<String,String> getTheMap(ArrayList<String> indice,ArrayList<String> expr){
        Map<String,String> indice_expr=new HashMap<>();
        for (int i=0;i<Math.min(indice.size(),expr.size());i++){
            indice_expr.put(indice.get(i),expr.get(i));
        }
        return indice_expr;
    }

}
