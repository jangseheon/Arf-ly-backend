package com.capstone.arfly.common.exception;

public class EmptyTokenException extends BusinessException {
    public EmptyTokenException(){
        super(ErrorCode.EMPTY_TOKEN);
    }
}
