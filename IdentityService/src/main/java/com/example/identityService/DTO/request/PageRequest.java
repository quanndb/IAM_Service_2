package com.example.identityService.DTO.request;

import com.example.identityService.DTO.EnumSortDirection;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageRequest {
    private int page = 1;
    private int size = 10;
    private String query = "";
    private String sortedBy = "id";
    private EnumSortDirection sortDirection = EnumSortDirection.DESC;
}
