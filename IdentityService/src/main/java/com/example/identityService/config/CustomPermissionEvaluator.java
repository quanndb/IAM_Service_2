package com.example.identityService.config;

import com.example.identityService.DTO.PermissionScope;
import com.example.identityService.entity.Account;
import com.example.identityService.entity.Permission;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.repository.AccountRepository;
import com.example.identityService.repository.AccountRoleRepository;
import com.example.identityService.repository.PermissionRepository;
import com.example.identityService.repository.RolePermissionRepository;
import com.example.identityService.service.AccountRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final AccountRepository accountRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AccountRoleService accountRoleService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {

        if(targetDomainObject instanceof String resourceName) {
            Permission foundResource = permissionRepository.findByNameIgnoreCase(resourceName)
                    .orElseThrow(()-> new AppExceptions(ErrorCode.PERMISSION_NOTFOUND));
            return checkUserPermission(authentication.getName(), foundResource.getId(), permission);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return checkUserPermission(authentication.getName(), targetId, permission);
    }

    private boolean checkUserPermission(String email, Object resourceId, Object permissionScope) {
        PermissionScope foundScope = PermissionScope.valueOf(permissionScope.toString().toUpperCase());

        Account foundUser = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppExceptions(ErrorCode.NOTFOUND_EMAIL));

        List<String> roleIds = accountRoleService.getAllUserRoleId(foundUser.getId());

        for(String item : roleIds){
            boolean isExistedRole =  rolePermissionRepository
                    .existsByRoleIdAndPermissionIdAndScope(
                            item,
                            resourceId.toString(),
                            foundScope
                    );

            if(isExistedRole) return true;
        }
        return false;
    }
}
