package com.example.identityService.Util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonMapper {
    private final ObjectMapper mapper;
    public <T> T JSONToObject(String JSON, Class<T> type) throws JsonProcessingException {
        return mapper.readValue(JSON, type);
    }

    public <T> List<T> JSONToList(String JSON, Class<T> type) throws JsonProcessingException {
        return mapper.readValue(JSON, mapper.getTypeFactory().constructCollectionType(List.class, type));
    }
}
