package com.project.shopapp.exceptions;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.utils.MessageKeys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter

public enum ErrorCode {


    INVALID_KEY(1000, "error.invalid_key", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(1001, "error.invalid_username", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1002, "error.invalid_password", HttpStatus.BAD_REQUEST),
    INVALID_NAME_ROLE(1003, "error.invalid_name_role", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1004, "error.user_existed", HttpStatus.CONFLICT),
    USER_IS_LOCKED(1005, "error.user_is_locked", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(1005, "error.user_not_existed", HttpStatus.NOT_FOUND),
    INVALID_USERNAME_OR_PASSWORD(1006, "error.invalid_username_or_password", HttpStatus.BAD_REQUEST),
    INVALID_NAME_PARAM(1007, "error.invalid_name_param", HttpStatus.BAD_REQUEST),
    INTERNAL_SERVER_ERROR(1008, "error.internal_server_error", HttpStatus.INTERNAL_SERVER_ERROR),
    COMMENT_ERROR(1009, "error.comment_error", HttpStatus.BAD_REQUEST),
    INVALID_RETYPE_PASSWORD(1010, "error.invalid_retype_password", HttpStatus.BAD_REQUEST),
    EXPIRATION_TOKEN(9990, "Token expired !", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(9991, "Token invalid !", HttpStatus.REQUEST_TIMEOUT),
    UNAUTHORIZED(9992, "User is not permitted !", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(9993, "Unauthenticated error !", HttpStatus.UNAUTHORIZED);
    private final int code;
    private final String message;
    private final HttpStatus httpStatus;


    ErrorCode(int code, String message, HttpStatus httpStatusCode) {
        //this.message =  LocalizationUtils.getInstance().getLocalizedMessage(message);

        this.code = code;
        this.message=message;
        this.httpStatus = httpStatusCode;

    }

}