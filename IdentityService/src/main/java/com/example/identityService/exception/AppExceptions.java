package com.example.identityService.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppExceptions extends RuntimeException{

    private ErrorCode errorCode;

    public AppExceptions(ErrorCode errorCode){
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
