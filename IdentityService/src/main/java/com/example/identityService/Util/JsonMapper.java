package com.example.identityService.Util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonMapper {
    private final ObjectMapper mapper;
    public <T> T JSONToObject(String JSON, Class<T> type) throws JsonProcessingException {
        return mapper.readValue(JSON, type);
    }
}
