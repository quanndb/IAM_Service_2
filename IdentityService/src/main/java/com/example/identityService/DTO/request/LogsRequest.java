package com.example.identityService.DTO.request;

import lombok.Getter;

@Getter
public class LogsRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String query = "";
    private String sortedBy = "id";
    private String sortDirection = "DESC";
}
