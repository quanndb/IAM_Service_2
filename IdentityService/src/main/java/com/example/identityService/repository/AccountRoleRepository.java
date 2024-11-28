package com.example.identityService.repository;

import com.example.identityService.entity.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRoleRepository extends JpaRepository<AccountRole,String> {
    List<AccountRole> findAllByAccountId(String accountId);
}
