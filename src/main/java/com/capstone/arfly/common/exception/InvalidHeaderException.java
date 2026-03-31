package com.capstone.arfly.common.exception;

public class InvalidHeaderException extends BusinessException {
    public InvalidHeaderException(){
        super(ErrorCode.INVALID_HEADER);
    }
}
