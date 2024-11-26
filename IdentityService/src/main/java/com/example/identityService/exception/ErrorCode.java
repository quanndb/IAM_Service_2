package com.example.identityService.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(500, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(501, "Invalid key", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(401, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    FORBIDDEN_EXCEPTION(403,"You're unable to do this", HttpStatus.FORBIDDEN),

    //
    UNKNOWN_REQUEST(404, "Unknown request has been requested", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_PASSWORD(405, "Your email or password is invalid", HttpStatus.BAD_REQUEST),
    USER_EXISTED(406, "This email has been created", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(407, "Invalid email has been requested", HttpStatus.BAD_REQUEST),
    EMAIL_PASSWORD_NOT_BLANK(407, "Email and password cannot be blank", HttpStatus.BAD_REQUEST),
    FIELD_NOT_BLANK(408, "These field cannot be blank", HttpStatus.BAD_REQUEST),
    PASSWORD_AT_LEAST(409, "Password at least 8 character", HttpStatus.BAD_REQUEST),
    PASSWORD_MUST_DIFFERENCE(410, "The new password must be difference with the old one", HttpStatus.BAD_REQUEST),
    WRONG_PASSWORD(411, "Your password is incorrect", HttpStatus.BAD_REQUEST),
    //
    ROLE_NOTFOUND(409, "Cannot found this role", HttpStatus.BAD_REQUEST),
    PERMISSION_NOTFOUND(410, "Cannot found this permission", HttpStatus.BAD_REQUEST),
    ROLE_PERMISSION_NOTFOUND(411, "Cannot found this role-permission", HttpStatus.BAD_REQUEST),
    ROLE_PERMISSION_EXISTED(412, "This role permission existed", HttpStatus.BAD_REQUEST),
    NOTFOUND_EMAIL(413, "Cannot found your email", HttpStatus.BAD_REQUEST),
    NOT_VERIFY_ACCOUNT(414, "You're account is not verified", HttpStatus.BAD_REQUEST),
    TOO_MUCH_LOGIN_FAIL(415, "You're attemp login fail too much and not able to login with this account in 30min", HttpStatus.BAD_REQUEST),
    TOO_MUCH_FORGOT_PASSWORD_ATTEMPT(416, "Too much forgot password attempt", HttpStatus.BAD_REQUEST),
    UNKNOWN_IP_REQUESTED(417, "Unknown IP has been requested, please confirm in your email", HttpStatus.BAD_REQUEST)
    ;


    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
