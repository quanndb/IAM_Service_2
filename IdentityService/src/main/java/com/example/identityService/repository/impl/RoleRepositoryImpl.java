package com.example.identityService.repository.impl;

import com.example.identityService.DTO.EnumSortDirection;
import com.example.identityService.DTO.request.RolePageRequest;
import com.example.identityService.DTO.response.RoleResponse;
import com.example.identityService.Util.StrUtils;
import com.example.identityService.entity.Role;
import com.example.identityService.mapper.RoleMapper;
import com.example.identityService.repository.CustomRoleRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RoleRepositoryImpl implements CustomRoleRepository {
    private final RoleMapper roleMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<RoleResponse> search(RolePageRequest request) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select a from Role a " + createWhereQuery(request, values) + createOrderQuery(request.getSortedBy(), request.getSortDirection());
        Query query = entityManager.createQuery(sql, Role.class);
        values.forEach(query::setParameter);
        query.setFirstResult((request.getPage() - 1) * request.getSize());
        query.setMaxResults(request.getSize());
        var res = query.getResultList();
        return roleMapper.toListRoleResponse(res);
    }

    @Override
    public Long count(RolePageRequest request) {
        Map<String, Object> values = new HashMap<>();
        String sql = "select count(a) from Role a " + createWhereQuery(request, values);
        Query query = entityManager.createQuery(sql, Long.class);
        values.forEach(query::setParameter);
        return (Long) query.getSingleResult();
    }

    private String createWhereQuery(RolePageRequest request, Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();
        sql.append(" where a.deleted = false");

        if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
            sql.append(" and (")
                    .append(" lower(cast(unaccent(concat(a.id, ' ', a.name, ' ', a.description)) as String))")
                    .append(" like :keyword ")
                    .append(" )");
            values.put("keyword", StrUtils.encodeKeyword(request.getQuery()));
        }

        // Lặp qua tất cả các trường trong UserPageRequest để tạo các điều kiện khác
        Field[] fields = request.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            try {
                Object value = field.get(request);

                if (value != null && !(value instanceof String && ((String) value).trim().isEmpty())) {
                    String fieldName = field.getName();

                    if (value instanceof String) {
                        sql.append(" and lower(a.").append(fieldName).append(") like :").append(fieldName);
                        values.put(fieldName, "%" + value.toString().toLowerCase() + "%");
                    } else {
                        sql.append(" and a.").append(fieldName).append(" = :").append(fieldName);
                        values.put(fieldName, value);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return sql.toString();
    }

    public StringBuilder createOrderQuery(String sortBy, EnumSortDirection sortDirection) {
        StringBuilder hql = new StringBuilder();

        String direction = (sortDirection == EnumSortDirection.ASC) ? "asc" : "desc";

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            hql.append(" order by a.").append(sortBy.trim()).append(" ").append(direction);
        } else {
            hql.append(" order by a.updatedDate desc");
        }
        return hql;
    }
}
