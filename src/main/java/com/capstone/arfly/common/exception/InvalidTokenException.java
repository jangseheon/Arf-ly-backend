package com.capstone.arfly.common.exception;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException(){
        super(ErrorCode.INVALID_TOKEN);
    }
}
