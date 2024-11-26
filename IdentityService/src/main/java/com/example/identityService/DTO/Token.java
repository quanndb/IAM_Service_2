package com.example.identityService.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Token {
    private String value;
    private Integer lifeTime;
}
