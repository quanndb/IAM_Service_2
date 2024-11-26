package com.example.identityService.service;

import com.example.identityService.DTO.response.PageResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class PageService<T> {
    private final EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public PageResponse<T> pageResponse(StoredProcedureQuery storedProcedureQuery){
        storedProcedureQuery.execute();
        List<T> response = storedProcedureQuery.getResultList();
        Integer totalRecords = (Integer) storedProcedureQuery.getOutputParameterValue("total_records");
        Integer page = (Integer) storedProcedureQuery.getParameterValue("p_page");
        Integer size = (Integer) storedProcedureQuery.getParameterValue("p_size");
        String query = (String) storedProcedureQuery.getParameterValue("p_query");
        String sortedBy = (String) storedProcedureQuery.getParameterValue("p_sorted_by");
        String sortDirection = (String) storedProcedureQuery.getParameterValue("p_sort_direction");
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
        storedProcedureQuery.registerStoredProcedureParameter("p_page", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("p_size", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("p_query", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("p_sorted_by", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("p_sort_direction", String.class, ParameterMode.IN);

        storedProcedureQuery.setParameter("result_cursor", null);
        storedProcedureQuery.setParameter("p_page", page);
        storedProcedureQuery.setParameter("p_size", size);
        storedProcedureQuery.setParameter("p_query", query);
        storedProcedureQuery.setParameter("p_sorted_by", sortedBy);
        storedProcedureQuery.setParameter("p_sort_direction", sortDirection);
        return storedProcedureQuery;
    }

    public <K> void addField(StoredProcedureQuery sp, String fieldName, Class<K> typeInput, K value) {
        sp.registerStoredProcedureParameter(fieldName, typeInput, ParameterMode.IN);
        sp.setParameter(fieldName, value);
    }
}
