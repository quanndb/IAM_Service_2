package com.example.identityService.DTO.response;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class PageResponse<T> {
    private int page;
    private int size;
    private int totalPages;
    private int totalRecords;
    private boolean isLast;
    private boolean isFirst;
    private String query;
    private String sortedBy;
    private String sortDirection;
    private List<T> response;
}
