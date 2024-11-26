package com.example.identityService.DTO.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class EmailRequest {
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String subject;
    @NotBlank(message = "FIELD_NOT_BLANK")
    private String content;
    private List<String> recipients;
}
