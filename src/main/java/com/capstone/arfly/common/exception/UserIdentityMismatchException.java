package com.capstone.arfly.common.exception;

public class UserIdentityMismatchException extends BusinessException {
    public UserIdentityMismatchException(){
        super(ErrorCode.USER_IDENTITY_MISMATCH);
    }
}
