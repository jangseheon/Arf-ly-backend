package com.capstone.arfly.common.exception;

public class UserNotExistsException extends BusinessException {
    public UserNotExistsException() {
        super(ErrorCode.USER_NOT_EXISTS);
    }
}
