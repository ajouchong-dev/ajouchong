package com.ajouchong.exception;

import com.ajouchong.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

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
        return ResponseEntity.badRequest().body(new ApiResponse<>(0, "유효성 검사에 실패했습니다.", errors));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleDuplicateEmailException(DuplicateEmailException ex) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("errCode", "duplicate_email");
        errorData.put("errMsg", ex.getMessage());
        return ResponseEntity.badRequest().body(new ApiResponse<>(0, "중복된 이메일입니다.", errorData));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("errCode", "file_size_exceeded");
        errorData.put("errMsg", "파일 크기가 최대 허용 크기(100MB)를 초과했습니다.");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ApiResponse<>(0, "파일 크기 제한을 초과했습니다.", errorData));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleGeneralException(Exception ex) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("errCode", "unknown_error");
        errorData.put("errMsg", "알 수 없는 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(0, "서버 오류가 발생했습니다.", errorData));
    }
}
