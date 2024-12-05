package com.example.identityService.controller;

import com.example.identityService.DTO.ApiResponse;
import com.example.identityService.DTO.request.AssignRoleRequest;
import com.example.identityService.DTO.request.CreateAccountRequest;
import com.example.identityService.DTO.request.SetUserEnableRequest;
import com.example.identityService.DTO.request.UserPageRequest;
import com.example.identityService.DTO.response.PageResponse;
import com.example.identityService.DTO.response.UserResponse;
import com.example.identityService.service.AccountRoleService;
import com.example.identityService.service.AccountService;
import com.example.identityService.service.auth.AbstractAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final AccountRoleService accountRoleService;

    @GetMapping
    @PreAuthorize("hasPermission('ACCOUNTS', 'READ')")
    public ApiResponse<PageResponse<UserResponse>> getAccounts(@ParameterObject UserPageRequest request) {

        return ApiResponse.<PageResponse<UserResponse>>builder()
                .code(200)
                .result(accountService.getUsers(request))
                .build();
    }

    @PostMapping
    @PreAuthorize("hasPermission('ACCOUNTS', 'CREATE')")
    public ApiResponse<?> createAccount(@RequestBody @Valid CreateAccountRequest request){
        boolean result = AbstractAuthService.createUser(request);
        return ApiResponse.builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasPermission('ACCOUNTS', 'READ')")
    public ApiResponse<?> getAccount(@PathVariable String accountId){
        return ApiResponse.builder()
                .code(200)
                .result(accountService.getUserinfo(accountId))
                .build();
    }

    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasPermission('ACCOUNTS', 'DELETE')")
    public ApiResponse<?> deleteAccount(@PathVariable String accountId){
        boolean result = accountService.deleteUser(accountId);
        return ApiResponse.builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @PostMapping("/{accountId}")
    @PreAuthorize("hasPermission('ACCOUNTS', 'UPDATE')")
    public ApiResponse<?> setEnableAccount(@RequestBody @Valid SetUserEnableRequest request, @PathVariable String accountId){
        boolean result = accountService.setUserEnable(accountId, request.isEnable());
        return ApiResponse.builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @PostMapping("/{accountId}/roles")
    @PreAuthorize("hasPermission('ROLES', 'CREATE')")
    public ApiResponse<?> assignRolesForUser(@RequestBody @Valid AssignRoleRequest request, @PathVariable String accountId){
        boolean result = accountRoleService.assignRolesForUser(accountId, request.getRoles());
        return ApiResponse.builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @DeleteMapping("/{accountId}/roles")
    @PreAuthorize("hasPermission('ROLES', 'DELETE')")
    public ApiResponse<?> unassignRolesForUser(@RequestBody @Valid AssignRoleRequest request, @PathVariable String accountId){
        boolean result = accountRoleService.unassignRolesForUser(accountId, request.getRoles());
        return ApiResponse.builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }
}
