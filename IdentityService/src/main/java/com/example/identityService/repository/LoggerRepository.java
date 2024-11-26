package com.example.identityService.repository;

import com.example.identityService.entity.Logs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggerRepository extends JpaRepository<Logs, String> {
    boolean existsByEmailAndIp(String email, String ip);
}
