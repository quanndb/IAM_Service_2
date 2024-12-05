package com.example.identityService.repository;

import com.example.identityService.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String>, CustomRoleRepository {
    Optional<Role> findByNameIgnoreCase(String roleName);
    List<Role> findAllByNameIn(List<String> names);
}
