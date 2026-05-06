package com.capstone.arfly.common.exception;

public class EmptyTermsAgreementException extends BusinessException {
    public EmptyTermsAgreementException() {
        super(ErrorCode.EMPTY_TERMS_AGREEMENT);
    }
}
