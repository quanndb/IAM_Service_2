package com.example.identityService.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LoggerController {

//    private final LoggerService loggerService;
//
//    @GetMapping
//    public ApiResponse<Object> getLogs(@RequestBody LogsRequest logsRequest){
//        return ApiResponse.builder()
//                .code(200)
//                .result(loggerService.getLoggers(logsRequest.getPage(),
//                        logsRequest.getSize(),
//                        logsRequest.getQuery(),
//                        logsRequest.getSortedBy(),
//                        logsRequest.getSortDirection()))
//                .build();
//    }
}
