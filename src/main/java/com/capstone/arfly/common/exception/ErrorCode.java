package com.capstone.arfly.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 회원입니다.", "USER_ALREADY_EXISTS"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다.", "INVALID_CREDENTIALS"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.", "INVALID_TOKEN"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다.", "TOKEN_EXPIRED"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 전화번호입니다.", "PHONE_ALREADY_EXISTS"),

    TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "폐기된 토큰입니다.", "TOKEN_REVOKED"),
    INVALID_HEADER(HttpStatus.BAD_REQUEST, "Authorization 헤더 형식이 올바르지 않습니다.", "INVALID_HEADER"),
    EMPTY_TOKEN(HttpStatus.BAD_REQUEST, "토큰 정보가 비어있습니다.", "EMPTY_TOKEN"),
    MISSING_TOKEN_INFO(HttpStatus.BAD_REQUEST, "토큰 내 필수 정보(UID/전화번호)가 누락되었습니다.", "MISSING_TOKEN_INFO"),
    OAUTH_ACCESS_TOKEN_FAIL(HttpStatus.BAD_REQUEST,"Oauth AccessToken 발급에 실패했습니다.","INVALID_METADATA"),
    USER_NOT_EXISTS(HttpStatus.NOT_FOUND,"요청하신 사용자는 존재하지 않습니다.","USER_NOT_EXISTS"),
    USER_IDENTITY_MISMATCH(HttpStatus.BAD_REQUEST,"사용자의 신원 정보가 일치하지 않습니다.","USER_INFO_NOT_MISMATCH"),

    BREED_NOT_FOUND(HttpStatus.BAD_REQUEST,"품종을 찾을 수 없습니다.","BREED_NOT_FOUND"),

    FILE_EMPTY(HttpStatus.BAD_REQUEST,"업로드할 파일이 없습니다.","FILE_EMPTY"),
    FILENAME_MISSING(HttpStatus.BAD_REQUEST,"파일명이 존재하지 않습니다.","FILENAME_MISSING"),
    FILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"현재 파일과 관련하여 서버에 문제가 있습니다.","FILE_NOT_FOUND"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,"파일 업로드에 실패했습니다.","FILE_UPLOAD_FAILED"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST,"잘못된 파일 유형입니다.","INVALID_FILE_TYPE"),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST,"파일 용량을 초과했습니다.", "FILE_TOO_LARGE");

    //예외 상태 코드
    private final HttpStatus httpStatus;
    //예외 메세지
    private final String message;
    //정의 예외 코드
    private final String errorCode;

    ErrorCode(HttpStatus httpStatus, String message, String errorCode) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.errorCode = errorCode;
    }
}
