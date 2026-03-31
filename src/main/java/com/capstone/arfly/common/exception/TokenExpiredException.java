package com.capstone.arfly.common.exception;

public class TokenExpiredException extends BusinessException {
    public TokenExpiredException(){
        super(ErrorCode.TOKEN_EXPIRED);
    }
}
