package com.example.identityService.exception;

import com.example.identityService.DTO.ApiResponse;
import com.fasterxml.jackson.core.JsonParseException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.Objects;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<?>> handleRuntimeExceptions(Exception exception){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode())
                .message(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage())
                .result(exception.getMessage())
                .build();
        log.error(exception.toString());
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_EXCEPTION.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<?>> handleForbiddenExceptions(){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.FORBIDDEN_EXCEPTION.getCode())
                .message(ErrorCode.FORBIDDEN_EXCEPTION.getMessage())
                .build();
        return ResponseEntity.status(ErrorCode.FORBIDDEN_EXCEPTION.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AuthorizationDeniedException.class)
    ResponseEntity<ApiResponse<?>> handleAuthorDeniedExceptions(){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.FORBIDDEN_EXCEPTION.getCode())
                .message(ErrorCode.FORBIDDEN_EXCEPTION.getMessage())
                .build();
        return ResponseEntity.status(ErrorCode.FORBIDDEN_EXCEPTION.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = BadJwtException.class)
    ResponseEntity<ApiResponse<?>> handleBadJwtExceptions(){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(ErrorCode.UNAUTHENTICATED.getCode())
                .message(ErrorCode.UNAUTHENTICATED.getMessage())
                .build();
        return ResponseEntity.status(ErrorCode.UNAUTHENTICATED.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = AppExceptions.class)
    ResponseEntity<ApiResponse<?>> handleAppExceptions(AppExceptions exceptions){
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(exceptions.getErrorCode().getCode())
                .message(exceptions.getErrorCode().getMessage())
                .build();
        return ResponseEntity.status(exceptions.getErrorCode().getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException exception){
        String enumKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try{
            errorCode = ErrorCode.valueOf(enumKey);
        }
        catch (Exception ignored){}
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = JsonParseException.class)
    ResponseEntity<ApiResponse<?>> handleJsonParseException(JsonParseException exception){
        ErrorCode errorCode = ErrorCode.UNKNOWN_REQUEST;
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .result(exception.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    ResponseEntity<ApiResponse<?>> handleNoResourceException(NoResourceFoundException exception){
        ErrorCode errorCode = ErrorCode.UNKNOWN_REQUEST;
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .result(exception.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    ResponseEntity<ApiResponse<?>> handleValidationsException(ConstraintViolationException exception){
        String[] details = Objects.requireNonNull(exception
                .getMessage()).split(" ");
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        try{
            errorCode = ErrorCode.valueOf(details[1]);
        }
        catch (Exception ignored){}
        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(details[0] + errorCode.getMessage())
                .build();
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }
}
