package com.example.identityService.service;

import com.example.identityService.DTO.EnumSortDirection;
import com.example.identityService.DTO.request.CreateAccountRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasPermission('ACCOUNTS', 'READ')")
    public UserResponse getUserinfo(String accountId){
        Account foundAccount = accountRepository.findById(accountId)
                .orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        List<String> roles = accountRoleService.getAllUserRole(foundAccount.getId());
        UserResponse response = accountMapper.toUserResponse(foundAccount);
        response.setRoles(roles);
        return response;
    }

    @PreAuthorize("hasPermission('ACCOUNTS', 'CREATE')")
    public boolean createUser(CreateAccountRequest request){
        accountRepository
                .findByEmail(request.getEmail())
                .ifPresent(_ -> {
                    throw new AppExceptions(ErrorCode.USER_EXISTED);
                });
        Account newAccount = accountMapper.toAccount(request);
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));

        Account savedAccount = accountRepository.save(newAccount);

        return accountRoleService
                .assignRolesForUser(savedAccount.getId(), request.getRoles()) &&
                keycloakService.createKeycloakUser(accountMapper.toRegisterRequest(request));
    }

    @PreAuthorize("hasPermission('ACCOUNTS', 'UPDATE')")
    public boolean setUserEnable(String accountId, boolean enable){
        Account foundAccount = accountRepository.findById(accountId)
                .orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        foundAccount.setEnable(enable);

        accountRepository.save(foundAccount);
        return true;
    }

    @PreAuthorize("hasPermission('ACCOUNTS', 'DELETE')")
    public boolean deleteUser(String accountId) {
        Account foundAccount = accountRepository.findById(accountId)
                .orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        foundAccount.setDeleted(true);

        accountRepository.save(foundAccount);
        return true;
    }

    @PreAuthorize("hasPermission('ACCOUNTS', 'READ')")
    public PageResponse<UserResponse> getUsers(int page, int size, String query, String sortedBy, EnumSortDirection sortDirection) throws JsonProcessingException {
        var res = accountRepository.getAccountData(page, size, query, sortedBy, sortDirection.name());
        int totalRecords = (int) res.getFirst()[0];
        String accountsJson = (String) res.getFirst()[1];
        List<UserResponse> userResponseList = jsonMapper
                .JSONToList(accountsJson == null? "[]" : accountsJson, UserResponse.class);
        return PageResponse.<UserResponse>builder()
                .page(page)
                .size(size)
                .query(query)
                .sortedBy(sortedBy)
                .sortDirection(sortDirection.name())
                .isFirst(page == 1)
                .isLast(page % size == page)
                .totalRecords(totalRecords)
                .totalPages(page % size)
                .response(userResponseList)
                .build();
    }
}
