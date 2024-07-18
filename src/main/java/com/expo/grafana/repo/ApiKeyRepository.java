package com.expo.grafana.repo;

import com.expo.grafana.model.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {

}
