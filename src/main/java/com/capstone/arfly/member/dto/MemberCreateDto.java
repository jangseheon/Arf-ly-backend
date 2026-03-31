package com.capstone.arfly.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Schema(description = "회원가입 요청 DTO")
public class MemberCreateDto {

    @Schema(description = "사용자 아이디 (영문/숫자 조합, 4~20자)", example = "member1234", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(regexp = "^[A-Za-z0-9]{4,20}$", message = "아이디는 영문자와 숫자 조합의 4~20자여야 합니다.")
    private String userId;

    @Schema(description = "비밀번호 (8~20자)", example = "password1234!", requiredMode = RequiredMode.REQUIRED)
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String password;

    //나중에 notNull 필요
    @Schema(description = "파이어베이스 인증 정보 (UID 및 전화번호)", requiredMode = RequiredMode.REQUIRED)
    @NotNull(message = "파이어베이스 토큰 정보가 누락되었습니다.")
    @Valid
    private FirebaseToken token;


    //필수 여부가 있을 경우 NotNull로 처리
    @Schema(description = "약관 동의 내역 리스트", requiredMode = RequiredMode.REQUIRED)
    @NotEmpty(message = "약관 동의 내역은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<UserAgreementDto> userAgreements;


}
