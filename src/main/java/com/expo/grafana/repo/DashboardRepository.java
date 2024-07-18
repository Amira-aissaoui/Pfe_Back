package com.expo.grafana.repo;

import com.expo.grafana.model.DashboardManager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardRepository extends JpaRepository<DashboardManager.Dashboard, Long> {
   // Optional findDashboardByTitle();
}
