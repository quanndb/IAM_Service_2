package com.example.identityService.controller;

import com.example.identityService.DTO.ApiResponse;
import com.example.identityService.DTO.request.CreateAccountRequest;
import com.example.identityService.DTO.request.SetUserEnableRequest;
import com.example.identityService.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ApiResponse<?> createAccount(@RequestBody @Valid CreateAccountRequest request){
        boolean result = accountService.createUser(request);
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
}
