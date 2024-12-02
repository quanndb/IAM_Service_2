package com.example.identityService.controller;

import com.example.identityService.DTO.ApiResponse;
import com.example.identityService.DTO.request.AssignRoleRequest;
import com.example.identityService.DTO.request.CreateAccountRequest;
import com.example.identityService.DTO.request.SetUserEnableRequest;
import com.example.identityService.service.AccountRoleService;
import com.example.identityService.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping
    public ApiResponse<?> createAccount(@RequestBody @Valid CreateAccountRequest request){
        boolean result = accountService.createUser(request);
        return ApiResponse.builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @GetMapping("/{accountId}")
    public ApiResponse<?> getAccount(@PathVariable String accountId){
        return ApiResponse.builder()
                .code(200)
                .result(accountService.getUserinfo(accountId))
                .build();
    }

    @DeleteMapping("/{accountId}")
    public ApiResponse<?> deleteAccount(@PathVariable String accountId){
        boolean result = accountService.deleteUser(accountId);
        return ApiResponse.builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @PostMapping("/{accountId}")
    public ApiResponse<?> setEnableAccount(@RequestBody @Valid SetUserEnableRequest request, @PathVariable String accountId){
        boolean result = accountService.setUserEnable(accountId, request.isEnable());
        return ApiResponse.builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @PostMapping("/{accountId}/roles")
    public ApiResponse<?> assignRolesForUser(@RequestBody @Valid AssignRoleRequest request, @PathVariable String accountId){
        boolean result = accountRoleService.assignRolesForUser(accountId, request.getRoles());
        return ApiResponse.builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }

    @DeleteMapping("/{accountId}/roles")
    public ApiResponse<?> unassignRolesForUser(@RequestBody @Valid AssignRoleRequest request, @PathVariable String accountId){
        boolean result = accountRoleService.unassignRolesForUser(accountId, request.getRoles());
        return ApiResponse.builder()
                .code(200)
                .message(ApiResponse.setResponseMessage(result))
                .build();
    }
}
