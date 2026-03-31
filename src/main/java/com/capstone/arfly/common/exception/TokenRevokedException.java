package com.capstone.arfly.common.exception;

public class TokenRevokedException extends BusinessException {
    public TokenRevokedException(){
        super(ErrorCode.TOKEN_REVOKED);
    }
}
