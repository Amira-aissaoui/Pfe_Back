package com.expo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.expo.grafana.model.ApiKeyEntity;
import com.expo.grafana.repo.ApiKeyRepository;
import com.expo.grafana.service.ApiKeyUpdater;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

class ApiKeyUpdaterTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @InjectMocks
    private ApiKeyUpdater apiKeyUpdater;
    @Value("${grafana.url}")
    String grafanaurl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAPIKey_ShouldReturnExistingApiKey() {
        // Given
        String apiKeyValue = "test-api-key";
        ApiKeyEntity apiKeyEntity = new ApiKeyEntity();
        apiKeyEntity.setId(1L);
        apiKeyEntity.setApiKey(apiKeyValue);
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(apiKeyEntity));

        // When
        String apiKey = apiKeyUpdater.getAPIKey();

        // Then
        assertEquals(apiKeyValue, apiKey);
        verify(apiKeyRepository, times(1)).findById(1L);
    }

    @Test
    void getAPIKey_ShouldReturnNull_WhenApiKeyNotInDatabase() {
        // Given
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        String apiKey = apiKeyUpdater.getAPIKey();

        // Then
        assertNull(apiKey);
        verify(apiKeyRepository, times(1)).findById(1L);
    }
/*
    @Test
    void setAPIKey_ShouldSetNewApiKey_WhenApiKeyIsNull() {
        // Given
        String apiKeyValue = "new-api-key";
        ApiKeyResponse apiKeyResponse = new ApiKeyResponse();
        apiKeyResponse.setKey(apiKeyValue);
        RestTemplate restTemplate = mock(RestTemplate.class);
        apiKeyUpdater.apiKey = null;
        apiKeyUpdater.grafanaUrl = grafanaurl;
        apiKeyUpdater.grafanaUsername = "admin";
        apiKeyUpdater.grafanaPassword = "admin";
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(ApiKeyResponse.class)))
                .thenReturn(new ResponseEntity<>(apiKeyResponse, HttpStatus.OK));
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        String apiKey = apiKeyUpdater.setAPIKey();

        // Then
        assertEquals(apiKeyValue, apiKey);
        verify(apiKeyRepository, times(1)).findById(1L);
        verify(apiKeyRepository, times(1)).save(any(ApiKeyEntity.class));
    }*/

    @Test
    void setAPIKey_ShouldSetExistingApiKey_WhenApiKeyAlreadyExists() {
        // Given
        String existingApiKey = "existing-api-key";
        RestTemplate restTemplate = mock(RestTemplate.class);
        apiKeyUpdater.apiKey = existingApiKey;
        apiKeyUpdater.grafanaUrl = grafanaurl;
        apiKeyUpdater.grafanaUsername = "admin";
        apiKeyUpdater.grafanaPassword = "admin";
        when(apiKeyRepository.findById(1L)).thenReturn(Optional.of(new ApiKeyEntity()));
//        when(apiKeyRepository.findById(1L).orElse(any(ApiKeyEntity.class))).thenReturn(new ApiKeyEntity());

        // When
        String apiKey = apiKeyUpdater.setAPIKey();

        // Then
        assertEquals(existingApiKey, apiKey);
      //  verify(apiKeyRepository, times(1)).findById(1L);
        verify(apiKeyRepository, never()).save(any(ApiKeyEntity.class));
    }


}
