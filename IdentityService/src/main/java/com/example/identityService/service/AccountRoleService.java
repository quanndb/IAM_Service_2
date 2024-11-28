package com.example.identityService.service;

import com.example.identityService.repository.AccountRoleRepository;
import com.example.identityService.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountRoleService {

    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;

    public List<String> getAllUserRole(String accountId) {
        return accountRoleRepository.findAllByAccountId(accountId).stream()
                .map(item -> roleRepository.findById(item.getRoleId()))
                .filter(Optional::isPresent)
                .map(optionalRole -> optionalRole.get().getName())
                .collect(Collectors.toList());
    }

    public List<String> getAllUserRoleId(String accountId) {
        return accountRoleRepository.findAllByAccountId(accountId).stream()
                .map(item -> roleRepository.findById(item.getRoleId()))
                .filter(Optional::isPresent)
                .map(optionalRole -> optionalRole.get().getId())
                .collect(Collectors.toList());
    }
}
