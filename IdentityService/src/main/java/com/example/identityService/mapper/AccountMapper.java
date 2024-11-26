package com.example.identityService.mapper;

import com.example.identityService.DTO.request.RegisterRequest;
import com.example.identityService.DTO.request.UpdateProfileRequest;
import com.example.identityService.entity.Account;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    Account toAccount(RegisterRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAccount(@MappingTarget Account response, UpdateProfileRequest request);
}
