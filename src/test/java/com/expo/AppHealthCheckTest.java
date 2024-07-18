package com.expo;

import com.expo.healthcheck.controller.AppHealthCheck;
import com.expo.healthcheck.service.AppHealthCheckService;
import com.expo.prometheus.service.PrometheusAlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AppHealthCheckTest {

    @Mock
    private AppHealthCheckService checkHealth;

    @Mock
    private PrometheusAlertService prometheusAlertService;

    @InjectMocks
    private AppHealthCheck appHealthCheck;
    @Value("${prometheus.server.url}")
    private String prometheusurl;
    @Value("${host}")
    private String host;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testPrometheusHealthCheck() {
        String prometheusUrl = prometheusurl;
        when(checkHealth.isServiceUp(eq(prometheusUrl))).thenReturn(true);

        ResponseEntity<Boolean> response = appHealthCheck.prometheusHealthCheck();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody());

        verify(checkHealth, times(1)).isServiceUp(eq(prometheusUrl));
    }


}
