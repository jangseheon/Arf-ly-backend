package com.capstone.arfly.common.exception;

import lombok.Getter;

@Getter
public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}
