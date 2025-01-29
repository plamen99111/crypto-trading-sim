package com.example.cryptosim.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class LoggingController {

    private static final Logger logger = LoggerFactory.getLogger(LoggingController.class);


    @PostMapping("/logs")
    public ResponseEntity<Void> logFrontendData(@RequestBody Map<String, Object> logData, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        logData.put("clientIp", clientIp); // Add IP to the log data

        logger.info("Frontend Log: {}", logData);
        return ResponseEntity.ok().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
