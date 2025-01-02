package com.project.shopapp.exceptions;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandle {
    private final LocalizationUtils localizationUtils;

    public GlobalExceptionHandle(LocalizationUtils localizationUtils) {
        this.localizationUtils = localizationUtils;
    }


@ExceptionHandler(DataNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)

public ResponseEntity<ApiResponse<ErrorCode>> handleNotFoundException(DataNotFoundException e) {

    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.<ErrorCode>builder()
                    .message(e.getMessage()).httpStatus(HttpStatus.NOT_FOUND).code(9999)
                    .build()
            );
}
    @ExceptionHandler(AppException.class)

    public ResponseEntity<ApiResponse<ErrorCode>> appExceptionHandler(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        String localizedMessage = localizationUtils.getLocalizedMessage(errorCode.getMessage());
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.<ErrorCode>builder()
                        .code(errorCode.getCode())
                        .message(localizedMessage)
                        .httpStatus(errorCode.getHttpStatus())
                        .build()
                );
    }
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<?>> handleGeneralException(Exception exception) {
        return ResponseEntity.internalServerError().body(
                ApiResponse.builder()
                        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .message(exception.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorCode>> accessDeniedExceptionHandler(AccessDeniedException e) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.<ErrorCode>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .httpStatus(errorCode.getHttpStatus())
                        .build()
                );
    }
//    @ExceptionHandler(JwtException.class)

    @ExceptionHandler(ExpiredTokenException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<ErrorCode>> accessDeniedExceptionHandler(JwtException e) {
        ErrorCode errorCode = ErrorCode.EXPIRATION_TOKEN;
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ApiResponse.<ErrorCode>builder()
                        .code(errorCode.getCode())
                        .message(e.getLocalizedMessage()+";/n"+errorCode.getMessage())
                        .httpStatus(errorCode.getHttpStatus())
                        .build()
                );
    }
}
