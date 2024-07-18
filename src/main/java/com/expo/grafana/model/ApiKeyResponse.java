package com.expo.grafana.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ApiKeyResponse {
    private Long id;
    private String name;
    private String key;
}
