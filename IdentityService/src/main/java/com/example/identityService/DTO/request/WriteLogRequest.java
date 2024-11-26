package com.example.identityService.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class WriteLogRequest {
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String actionName;
    private String note;
}
