package com.capstone.arfly.common.exception;

public class InvalidTermsIdException extends BusinessException {
    public InvalidTermsIdException() {
        super(ErrorCode.INVALID_TERMS_ID);
    }
}
