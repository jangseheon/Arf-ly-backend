package com.capstone.arfly.common.exception;

public class MissingTokenException extends BusinessException {
    public MissingTokenException(){
        super(ErrorCode.MISSING_TOKEN_INFO);
    }
}
