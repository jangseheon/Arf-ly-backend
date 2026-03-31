package com.capstone.arfly.common.exception;

public class PhoneAlreadyException extends BusinessException {
    public PhoneAlreadyException(){
        super(ErrorCode.PHONE_ALREADY_EXISTS);
    }
}
