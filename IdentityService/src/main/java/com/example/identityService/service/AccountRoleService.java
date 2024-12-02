package com.example.identityService.service;

import com.example.identityService.DTO.EnumRole;
import com.example.identityService.entity.AccountRole;
import com.example.identityService.entity.Role;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.repository.AccountRepository;
import com.example.identityService.repository.AccountRoleRepository;
import com.example.identityService.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountRoleService {

    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final AccountRepository accountRepository;

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

    @PreAuthorize("hasPermission('ROLES', 'CREATE')")
    public boolean assignRolesForUser(String accountId, List<EnumRole> roles){
        accountRepository.findById(accountId).orElseThrow(()-> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        List<AccountRole> accountRoleList = roles.stream()
                .map(item -> {
                    String roleId = roleRepository.findByNameIgnoreCase(item.getName())
                            .orElseThrow(() -> new AppExceptions(ErrorCode.ROLE_NOTFOUND))
                            .getId();
                    if (!accountRoleRepository.existsByAccountIdAndRoleId(accountId, roleId)) {
                        return AccountRole.builder()
                                .roleId(roleId)
                                .accountId(accountId)
                                .build();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();

        accountRoleRepository.saveAll(accountRoleList);

        return true;
    }

    @PreAuthorize("hasPermission('ROLES', 'DELETE')")
    public boolean unassignRolesForUser(String accountId, List<EnumRole> roles){
        accountRepository.findById(accountId).orElseThrow(()-> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));

        List<AccountRole> userRoles = accountRoleRepository.findAllByAccountId(accountId);
        List<String> deleteRoleList = roleRepository.findAllByNameIn(roles.stream()
                        .map(EnumRole::getName)
                        .toList()).stream()
                .map(Role::getId)
                .toList();

        List<AccountRole> deleteAccountRoleList = userRoles.stream()
                .filter(item -> deleteRoleList.contains(item.getRoleId()))
                .toList();

        accountRoleRepository.deleteAll(deleteAccountRoleList);

        return true;
    }
}
