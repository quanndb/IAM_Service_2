package com.example.identityService.service;

import com.example.identityService.DTO.EnumSortDirection;
import com.example.identityService.DTO.request.CreateAccountRequest;
import com.example.identityService.DTO.request.UserPageRequest;
import com.example.identityService.DTO.response.PageResponse;
import com.example.identityService.DTO.response.UserResponse;
import com.example.identityService.Util.JsonMapper;
import com.example.identityService.entity.Account;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.mapper.AccountMapper;
import com.example.identityService.repository.AccountRepository;
import com.example.identityService.service.auth.KeycloakService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountRoleService accountRoleService;
    private final KeycloakService keycloakService;
    private final JsonMapper jsonMapper;

    public UserResponse getUserinfo(String accountId){
        Account foundAccount = accountRepository.findById(accountId)
                .orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        List<String> roles = accountRoleService.getAllUserRole(foundAccount.getId());
        UserResponse response = accountMapper.toUserResponse(foundAccount);
        response.setRoles(roles);
        return response;
    }

    public boolean setUserEnable(String accountId, boolean enable){
        Account foundAccount = accountRepository.findById(accountId)
                .orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        foundAccount.setEnable(enable);

        accountRepository.save(foundAccount);
        return true;
    }

    public boolean deleteUser(String accountId) {
        Account foundAccount = accountRepository.findById(accountId)
                .orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        foundAccount.setDeleted(true);

        accountRepository.save(foundAccount);
        return true;
    }

    public PageResponse<UserResponse> getUsers(UserPageRequest request) {
        long totalRecords = accountRepository.count(request);
        List<UserResponse> userResponseList = accountRepository.search(request);
        return PageResponse.<UserResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .query(request.getQuery())
                .sortedBy(request.getSortedBy())
                .sortDirection(request.getSortDirection().name())
                .first(request.getPage() == 1)
                .last(request.getPage() % request.getSize() == request.getPage())
                .totalRecords(totalRecords)
                .totalPages(request.getPage() % request.getSize())
                .response(userResponseList)
                .build();
    }
}
