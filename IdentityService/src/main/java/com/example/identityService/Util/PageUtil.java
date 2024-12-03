package com.example.identityService.Util;

import com.example.identityService.DTO.response.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class PageUtil<T> {
    private final EntityManager entityManager;

    public PageResponse<T> pageResponse(StoredProcedureQuery storedProcedureQuery){
        storedProcedureQuery.execute();
        List<T> response = storedProcedureQuery.getResultList();
        Integer totalRecords = (Integer) storedProcedureQuery.getOutputParameterValue("total_records");
        Integer page = (Integer) storedProcedureQuery.getParameterValue("page");
        Integer size = (Integer) storedProcedureQuery.getParameterValue("size");
        String query = (String) storedProcedureQuery.getParameterValue("query");
        String sortedBy = (String) storedProcedureQuery.getParameterValue("sorted_by");
        String sortDirection = (String) storedProcedureQuery.getParameterValue("sort_direction");
        int totalPages = (int) Math.ceil((double) totalRecords / size);

        return PageResponse.<T>builder()
                .page(page)
                .size(size)
                .isLast(Objects.equals(page, totalPages))
                .isFirst(Objects.equals(page, 1))
                .query(query)
                .sortedBy(sortedBy)
                .sortDirection(sortDirection)
                .totalRecords(totalRecords)
                .totalPages(totalPages)
                .response(response)
                .build();
    }

    public StoredProcedureQuery prepareStatement(String storeName, Class<T> responseType,
                                                 Integer page, Integer size, String query,
                                                 String sortedBy, String sortDirection) {
        StoredProcedureQuery storedProcedureQuery = entityManager
                .createStoredProcedureQuery(storeName, responseType);
        storedProcedureQuery.registerStoredProcedureParameter("total_records", Integer.class, ParameterMode.OUT);
        storedProcedureQuery.registerStoredProcedureParameter("result_cursor", Object.class, ParameterMode.INOUT);
        storedProcedureQuery.registerStoredProcedureParameter("page", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("size", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("query", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("sorted_by", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("sort_direction", String.class, ParameterMode.IN);

        storedProcedureQuery.setParameter("result_cursor", null);
        storedProcedureQuery.setParameter("page", page);
        storedProcedureQuery.setParameter("size", size);
        storedProcedureQuery.setParameter("query", query);
        storedProcedureQuery.setParameter("sorted_by", sortedBy);
        storedProcedureQuery.setParameter("sort_direction", sortDirection);
        return storedProcedureQuery;
    }

    public <K> void addField(StoredProcedureQuery sp, String fieldName, Class<K> typeInput, K value) {
        sp.registerStoredProcedureParameter(fieldName, typeInput, ParameterMode.IN);
        sp.setParameter(fieldName, value);
    }
}
