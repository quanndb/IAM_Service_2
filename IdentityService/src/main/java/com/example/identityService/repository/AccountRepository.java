package com.example.identityService.repository;

import com.example.identityService.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    Optional<Account> findByEmail(String email);

    @Query(value = "SELECT * FROM get_account_data(:page, :size, :query, :sortedBy, :sortDirection)", nativeQuery = true)
    List<Object[]> getAccountData(
            @Param("page") int page,
            @Param("size") int size,
            @Param("query") String query,
            @Param("sortedBy") String sortedBy,
            @Param("sortDirection") String sortDirection);
}
