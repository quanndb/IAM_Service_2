package com.example.identityService.repository;

import com.example.identityService.DTO.request.UserPageRequest;
import com.example.identityService.DTO.response.UserResponse;

import java.util.List;

public interface CustomAccountRepository {
    List<UserResponse> search(UserPageRequest request);
    Long count(UserPageRequest request);
}
