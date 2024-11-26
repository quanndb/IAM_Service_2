package com.example.identityService.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class LoggerResponse {
    private String id;
    private String email;
    private String ip;
    private String actionName;
    private String dateTime;
    private String note;
}
