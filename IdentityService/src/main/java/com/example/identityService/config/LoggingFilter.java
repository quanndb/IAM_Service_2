package com.example.identityService.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Arrays;

@Component
public class LoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    private final String[] sensitiveFields = {
            "password",
            "currentPassword",
            "newPassword",
            "token",
            "accessToken",
            "refreshToken",
            "access_token",
            "refresh_token"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        try {
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            logRequest(wrappedRequest);

            logResponse(wrappedResponse);

            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String requestBody = "";
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                requestBody = maskSensitiveData(new String(content, request.getCharacterEncoding()));
            }
        } catch (Exception e) {
            logger.warn("Failed to read request body", e);
        }

        StringBuilder params = new StringBuilder();
                request.getParameterMap().forEach((key, value)
                -> params.append(key).append(":").append(Arrays.toString(value)).append(","));

        logger.info("Request - Method: {}, URI: {}, Params: {} Body: {}",
                request.getMethod(),
                request.getRequestURI(),
                params,
                requestBody);
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        String responseBody = "";
        try {
            byte[] content = response.getContentAsByteArray();
            if (content.length > 0) {
                responseBody = maskSensitiveData(new String(content, response.getCharacterEncoding()));
            }
        } catch (Exception e) {
            logger.warn("Failed to read response body", e);
        }

        logger.info("Response - Status: {}, Body: {}", response.getStatus(), responseBody);
    }

    private String maskSensitiveData(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        // Lặp qua từng trường và thay thế dữ liệu nhạy cảm
        for (String field : sensitiveFields) {
            String regex = "(?i)\"" + field + "\"\\s*:\\s*\"[^\"]*\"";
            String replacement = "\"" + field + "\":\"[PROTECTED]\"";
            data = data.replaceAll(regex, replacement);
        }
        return data;
    }
}
