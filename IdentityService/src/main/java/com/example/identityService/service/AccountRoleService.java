package com.example.identityService.service;

import com.example.identityService.entity.AccountRole;
import com.example.identityService.entity.Role;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.repository.AccountRepository;
import com.example.identityService.repository.AccountRoleRepository;
import com.example.identityService.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountRoleService {

    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final AccountRepository accountRepository;

    public List<String> getAllUserRole(String accountId) {
        List<String> foundRoleIds = accountRoleRepository.findAllByAccountIdAndDeletedIsFalse(accountId)
                .stream()
                .map(AccountRole::getRoleId)
                .toList();
        return roleRepository.findAllById(foundRoleIds).stream().map(Role::getName).toList();
    }

    public List<String> getAllUserRoleId(String accountId) {
        List<String> foundRoleIds = accountRoleRepository.findAllByAccountIdAndDeletedIsFalse(accountId)
                .stream()
                .map(AccountRole::getRoleId)
                .toList();
        return roleRepository.findAllById(foundRoleIds).stream().map(Role::getId).toList();
    }

    public boolean assignRolesForUser(String accountId, List<String> roles){
        accountRepository.findById(accountId).orElseThrow(()-> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        List<String> accountRoleIdList = accountRoleRepository.findAllByAccountIdAndDeletedIsFalse(accountId)
                .stream()
                .map(AccountRole::getRoleId)
                .toList();

        List<String> accountRoleNames = roleRepository.findAllById(accountRoleIdList)
                .stream()
                .map(Role::getName)
                .toList();

        List<AccountRole> saveAccountRoles = roleRepository.findAllByNameIn(roles.stream()
                        .filter(name -> !accountRoleNames.contains(name))
                        .toList())
                .stream()
                .map(item->AccountRole.builder()
                        .roleId(item.getId())
                        .accountId(accountId)
                        .build())
                .toList();

        accountRoleRepository.saveAll(saveAccountRoles);
        return true;
    }

    public boolean unassignRolesForUser(String accountId, List<String> roles){
        accountRepository.findById(accountId).orElseThrow(()-> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));

        List<AccountRole> userRoles = accountRoleRepository.findAllByAccountIdAndDeletedIsFalse(accountId);
        List<String> deleteRoleList = roleRepository.findAllByNameIn(roles.stream()
                        .toList()).stream()
                .map(Role::getId)
                .toList();

        List<AccountRole> deleteAccountRoleList = userRoles.stream()
                .filter(item -> deleteRoleList.contains(item.getRoleId()))
                .peek(item -> item.setDeleted(true))
                .toList();

        accountRoleRepository.saveAll(deleteAccountRoleList);
        return true;
    }
}
