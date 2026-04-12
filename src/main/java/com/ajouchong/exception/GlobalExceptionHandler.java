package com.ajouchong.exception;

import com.ajouchong.common.ApiResponse;
import com.ajouchong.oauth.OAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(new ApiResponse<>(0, "Validation failed.", errors));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleDuplicateEmailException(DuplicateEmailException ex) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("errCode", "duplicate_email");
        errorData.put("errMsg", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponse<>(0, "Duplicate email.", errorData));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("errCode", "file_size_exceeded");
        errorData.put("errMsg", "File size exceeds the 100MB limit.");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ApiResponse<>(0, "File size limit exceeded.", errorData));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("errCode", "invalid_request_body");
        errorData.put("errMsg", "Invalid JSON request body.");
        return ResponseEntity.badRequest().body(new ApiResponse<>(0, "Invalid request body.", errorData));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("errCode", "invalid_argument");
        errorData.put("errMsg", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponse<>(0, ex.getMessage(), errorData));
    }

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleOAuthException(OAuthException ex) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("errCode", "oauth_error");
        errorData.put("errMsg", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(0, ex.getMessage(), errorData));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception", ex);
        Map<String, String> errorData = new HashMap<>();
        errorData.put("errCode", "unknown_error");
        errorData.put("errMsg", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(0, "Server error occurred.", errorData));
    }
}
