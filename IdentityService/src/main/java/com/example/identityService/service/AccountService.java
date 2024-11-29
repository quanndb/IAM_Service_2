package com.example.identityService.service;

import com.example.identityService.DTO.EnumRole;
import com.example.identityService.DTO.request.CreateAccountRequest;
import com.example.identityService.entity.Account;
import com.example.identityService.entity.AccountRole;
import com.example.identityService.exception.AppExceptions;
import com.example.identityService.exception.ErrorCode;
import com.example.identityService.mapper.AccountMapper;
import com.example.identityService.repository.AccountRepository;
import com.example.identityService.repository.AccountRoleRepository;
import com.example.identityService.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;


    @PreAuthorize("hasPermission('accounts', 'CREATE')")
    public boolean createUser(CreateAccountRequest request){
        accountRepository
                .findByEmail(request.getEmail())
                .ifPresent(_ -> {
                    throw new AppExceptions(ErrorCode.USER_EXISTED);
                });
        Account newAccount = accountMapper.toAccount(request);
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));

        Account savedAccount = accountRepository.save(newAccount);

        String userRoleId = roleRepository.findByNameIgnoreCase(EnumRole.USER.name())
                .orElseThrow(()-> new AppExceptions(ErrorCode.ROLE_NOTFOUND)).getId();

        accountRoleRepository.save(AccountRole.builder()
                .accountId(savedAccount.getId())
                .roleId(userRoleId)
                .build());

        return true;
    }

    @PreAuthorize("hasPermission('accounts', 'UPDATE')")
    public boolean setUserEnable(String accountId, boolean enable){
        Account foundAccount = accountRepository.findById(accountId)
                .orElseThrow(()->new AppExceptions(ErrorCode.NOTFOUND_EMAIL));
        foundAccount.setEnable(enable);

        accountRepository.save(foundAccount);
        return true;
    }
}
