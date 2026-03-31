package com.capstone.arfly.common.exception;

public class OauthAccessTokenException extends BusinessException {
    public OauthAccessTokenException(){
        super(ErrorCode.OAUTH_ACCESS_TOKEN_FAIL);
    }
}
