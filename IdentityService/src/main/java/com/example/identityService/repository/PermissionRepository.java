package com.example.identityService.repository;

import com.example.identityService.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {
    Optional<Permission> findByCodeIgnoreCase(String name);
    List<Permission> findAllByCodeIgnoreCaseIn(List<String> codes);

    @Query(value = "SELECT * FROM get_permission_data(:page, :size, :query, :sortedBy, :sortDirection)", nativeQuery = true)
    List<Object[]> getPermissionData(
            @Param("page") int page,
            @Param("size") int size,
            @Param("query") String query,
            @Param("sortedBy") String sortedBy,
            @Param("sortDirection") String sortDirection);
}
