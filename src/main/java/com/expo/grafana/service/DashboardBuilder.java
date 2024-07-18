package com.expo.grafana.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

@Service
public class DashboardBuilder {

    public String buildDashboard(String title, String[] targets) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = objectMapper.createObjectNode();
        rootNode.put("title", title);
        System.out.println("l title" + title);
        rootNode.put("overwrite", true);
        ArrayNode panelsNode = objectMapper.createArrayNode();
        rootNode.set("panels", panelsNode);

        System.out.println("l json" + objectMapper.writeValueAsString(rootNode));
        return objectMapper.writeValueAsString(rootNode);
    }




}
