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

import java.io.IOException;

@Component
public class LoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        if (httpRequest.getParameterMap().containsKey("password")) {
            logger.info("Request contains sensitive data; hiding 'password'.");
        }

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        logger.info("Request - Method: {}, URI: {}, Params: {}",
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                httpRequest.getParameterMap());

        chain.doFilter(request, response);

        logger.info("Response - Status: {}", httpResponse.getStatus());
    }
}
