package com.example.identityService.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum EmailEnum {
    FORGOT_PASSWORD("Confirm your reset password process","Please click here to reset password"),
    VERIFY_EMAIL("Confirm your registration", "lease click here to confirm your registration"),
    RESET_PASSWORD_SUCCESS("Change password successfully", "Your password has been set at"),
    CONFIRM_IP("Confirm that is you","Please click here to confirm your IP")
    ;

    private final String subject;
    private final String content;
}
