package com.expo.security.repo;

import com.expo.security.model.User;
import com.expo.security.model.UserProjectRole;
import com.expo.security.model.UserRole;
import com.expo.security.model.UserRoleDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
 //   void deleteAll(List<UserRole> userRoles);
        List<UserRole> findByProjectIdAndUserId(Long projectID,Long userID);
    @Override
    void deleteAll();
}
