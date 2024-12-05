package com.example.identityService.mapper;

import com.example.identityService.DTO.request.CreateAccountRequest;
import com.example.identityService.DTO.request.RegisterRequest;
import com.example.identityService.DTO.request.UpdateProfileRequest;
import com.example.identityService.DTO.response.UserResponse;
import com.example.identityService.entity.Account;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    Account toAccount(RegisterRequest request);
    Account toAccount(CreateAccountRequest request);
    UserResponse toUserResponse(Account request);
    List<UserResponse> toListUserResponse(List<Account> request);

    RegisterRequest toRegisterRequest(CreateAccountRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccount(@MappingTarget Account response, UpdateProfileRequest request);
}
