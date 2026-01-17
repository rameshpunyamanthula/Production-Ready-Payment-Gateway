package com.gateway.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {

        String code = ex.getMessage();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String description = "Internal server error";

        switch (code) {

            case "AUTHENTICATION_ERROR":
                status = HttpStatus.UNAUTHORIZED;
                description = "Invalid API credentials";
                break;

            case "BAD_REQUEST_ERROR":
                status = HttpStatus.BAD_REQUEST;
                description = "Invalid request parameters";
                break;

            case "NOT_FOUND_ERROR":
                status = HttpStatus.NOT_FOUND;
                description = "Resource not found";
                break;

            case "INVALID_VPA":
                status = HttpStatus.BAD_REQUEST;
                description = "VPA format invalid";
                break;

            case "INVALID_CARD":
                status = HttpStatus.BAD_REQUEST;
                description = "Card validation failed";
                break;

            case "EXPIRED_CARD":
                status = HttpStatus.BAD_REQUEST;
                description = "Card expiry date invalid";
                break;

            case "PAYMENT_FAILED":
                status = HttpStatus.BAD_REQUEST;
                description = "Payment processing failed";
                break;
        }

        Map<String, Object> error = new HashMap<>();
        error.put("code", code);
        error.put("description", description);

        Map<String, Object> response = new HashMap<>();
        response.put("error", error);

        return ResponseEntity.status(status).body(response);
    }
}
