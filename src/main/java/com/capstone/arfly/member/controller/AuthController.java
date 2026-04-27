package com.capstone.arfly.member.controller;

import com.capstone.arfly.common.auth.JwtTokenUtil;
import com.capstone.arfly.common.exception.ErrorResponse;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.domain.SocialType;
import com.capstone.arfly.member.dto.AccessTokenRequestDto;
import com.capstone.arfly.member.dto.AccessTokenResponseDto;
import com.capstone.arfly.member.dto.GoogleAccessTokenDto;
import com.capstone.arfly.member.dto.GoogleProfileDto;
import com.capstone.arfly.member.dto.KakaoAccessTokenDto;
import com.capstone.arfly.member.dto.KakaoProfileDto;
import com.capstone.arfly.member.dto.LogoutRequestDto;
import com.capstone.arfly.member.dto.MemberCreateDto;
import com.capstone.arfly.member.dto.MemberLoginDto;
import com.capstone.arfly.member.dto.NaverAccessTokenDto;
import com.capstone.arfly.member.dto.NaverProfileDto;
import com.capstone.arfly.member.dto.NaverRedirectDto;
import com.capstone.arfly.member.dto.PasswordRequestDto;
import com.capstone.arfly.member.dto.PasswordResetRequestDto;
import com.capstone.arfly.member.dto.PasswordResponseDto;
import com.capstone.arfly.member.dto.PhoneAuthInfoDto;
import com.capstone.arfly.member.dto.RedirectDto;
import com.capstone.arfly.member.dto.TokenResponseDto;
import com.capstone.arfly.member.dto.UserIdResponseDto;
import com.capstone.arfly.member.service.AuthService;
import com.capstone.arfly.common.service.FirebaseService;
import com.capstone.arfly.member.service.GoogleService;
import com.capstone.arfly.member.service.KakaoService;
import com.capstone.arfly.member.service.NaverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "auth", description = "회원 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final JwtTokenUtil jwtTokenUtil;
    private final GoogleService googleService;
    private final KakaoService kakaoService;
    private final NaverService naverService;
    private final FirebaseService firebaseService;

    @Operation(summary = "회원가입", description = "사용자의 정보를 받아 회원가입 진행 후 토큰을 반환한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공 및 토큰 발급",
                    content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 유효성 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 아이디로 가입 시도",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<?> userCreate(@Valid @RequestBody MemberCreateDto memberCreateDto) {
        //신규 유저 생성
        Member member = authService.create(memberCreateDto);
        //토큰 생성
        TokenResponseDto response = authService.generateTokens(member);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "시스템 로그인", description = "사용자의 아이디와 비밀번호르 입력받고 토큰을 발급한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공 및 토큰 발급",
                    content = @Content(schema = @Schema(implementation = TokenResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(입력값 유효성 검증 실패)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호가 일치하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@Valid @RequestBody MemberLoginDto memberLoginDto) {
        //검증
        Member member = authService.login(memberLoginDto);
        //토큰 발급
        TokenResponseDto response = authService.generateTokens(member);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Operation(summary = "엑세스 토큰 재발급", description = "리프레시 토큰을 통해 엑세스 토큰을 재발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "엑세스 토큰 재발급 성공",
                    content = @Content(schema = @Schema(implementation = AccessTokenResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "토큰의 유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "토큰이 만료",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/token/refresh")
    public ResponseEntity<?> reissueToken(@Valid @RequestBody AccessTokenRequestDto accessTokenRequestDto) {
        //리프레시 토큰 검증
        Member member = authService.validateRefreshToken(accessTokenRequestDto);
        //엑세스 토큰 재발급
        String accessToken = jwtTokenUtil.createAccessToken(member.getId(), member.getRole().toString());
        AccessTokenResponseDto response = AccessTokenResponseDto.builder().accessToken(accessToken).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Operation(summary = "로그아웃 및 리프레시 토큰 무효화", description = "리프레시 토큰을 무효화한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 무효화 성공(Body 데이터 없음)"),
            @ApiResponse(responseCode = "401", description = "토큰의 유효성 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "토큰이 만료",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<?> doLogout(@Valid @RequestBody LogoutRequestDto logoutRequestDto) {
        //토큰 검증 및 삭제
        authService.logout(logoutRequestDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @Operation(
            summary = "구글 소셜 로그인",
            description = "구글 인가 코드를 이용해 사용자 정보를 가져오고, 회원가입 또는 로그인을 처리한 뒤 JWT 토큰을 발급."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인/회원가입 성공 및 토큰 발급",
                    content = @Content(schema = @Schema(implementation = TokenResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (1. 파라미터 유효성 검증 실패(@Valid) 2. 인가 코드 문제  3. 리다이렉트 URI 불일치)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "구글 인증 실패 (액세스 토큰 만료 또는 유효하지 않음)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류 또는 구글 서버 통신 불가",
                    content = @Content(schema = @Schema(implementation = RuntimeException.class))
            )
    })
    @PostMapping("/google/doLogin")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody RedirectDto redirectDto) {
        //accessToken 발급
        GoogleAccessTokenDto accessToken = googleService.getAccessToken(redirectDto);

        // 사용자 정보 얻기
        GoogleProfileDto googleProfile = googleService.getGoogleProfile(accessToken.getAccess_token());

        //회원가입이 되어 있지 않다면 회원가입
        Member originalMember = authService.getMemberBySocialId(googleProfile.getSub());
        if (originalMember == null) {
            originalMember = authService.createOauth(googleProfile.getSub(), googleProfile.getEmail(),
                    SocialType.GOOGLE, googleProfile.getName());
        }

        // 회원가입 돼 있는 회원이라면 토큰 발급
        TokenResponseDto response = authService.generateTokens(originalMember);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @Operation(
            summary = "카카오 소셜 로그인",
            description = "카카오 인가 코드를 이용해 사용자 정보를 조회하고, 회원가입 또는 로그인을 처리한 뒤 JWT 토큰을 발급."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인/회원가입 성공 및 토큰 발급",
                    content = @Content(schema = @Schema(implementation = TokenResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (1. 파라미터 유효성 검증 실패(@Valid) 2. 인가 코드 문제  3. 리다이렉트 URI 불일치)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "카카오 인증 실패 (유효하지 않거나 만료된 카카오 액세스 토큰)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류 또는 카카오 인증 서버 통신 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/kakao/doLogin")
    public ResponseEntity<?> kakaoLogin(@Valid @RequestBody RedirectDto redirectDto) {
        KakaoAccessTokenDto accessToken = kakaoService.getAccessToken(redirectDto);

        KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessToken.getAccess_token());

        Member originalMember = authService.getMemberBySocialId(kakaoProfileDto.getId());
        if (originalMember == null) {
            originalMember = authService.createOauth(kakaoProfileDto.getId(),
                    kakaoProfileDto.getKakao_account().getEmail(),
                    SocialType.KAKAO, kakaoProfileDto.getKakao_account().getProfile().getNickname());
        }

        TokenResponseDto response = authService.generateTokens(originalMember);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @Operation(
            summary = "네이버 소셜 로그인",
            description = "네이버 인가 코드를 이용해 사용자 정보를 조회하고, 회원가입 또는 로그인을 처리한 뒤 JWT 토큰을 발급."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인/회원가입 성공 및 토큰 발급",
                    content = @Content(schema = @Schema(implementation = TokenResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (1. 파라미터 유효성 검증 실패(@Valid) 2. 인가 코드 문제  3. 리다이렉트 URI 불일치)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "네이버 인증 실패 (유효하지 않거나 만료된 카카오 액세스 토큰)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류 또는 네이버 인증 서버 통신 불가",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/naver/doLogin")
    public ResponseEntity<?> naverLogin(@Valid @RequestBody NaverRedirectDto naverRedirectDto) {
        NaverAccessTokenDto accessToken = naverService.getAccessToken(naverRedirectDto);

        NaverProfileDto naverProfileDto = naverService.getNaverProfile(accessToken.getAccess_token());

        Member originalMember = authService.getMemberBySocialId(naverProfileDto.getResponse().getId());
        if (originalMember == null) {
            originalMember = authService.createOauth(naverProfileDto.getResponse().getId(),
                    naverProfileDto.getResponse().getEmail(),
                    SocialType.NAVER, naverProfileDto.getResponse().getName());
        }
        TokenResponseDto response = authService.generateTokens(originalMember);
        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    @Operation(
            summary = "토큰 검사 및 전화번호 중복 검사",
            description = "파이어베이스에서 추출한 토큰이 맞는지 검증하고, 토큰에 포함된 전화번호의 중복 여부를 확인. 헤더의 Bearer 토큰이 필수."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 성공(응답 바디 없음)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (헤더 형식 오류, 토큰 누락, 필수 정보 누락 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 만료, 폐기, 유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 가입된 전화번호 (전화번호 중복 발생)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/phone/verify")
    public ResponseEntity<Void> verifyPhoneNumber(
            @Parameter(name = "Authorization", description = "Bearer {Firebase_Token}", required = true)
            @RequestHeader("Authorization") String token) {
        // 토큰 검증 및 유저 정보 추출
        PhoneAuthInfoDto phoneAuthInfo = firebaseService.verifyTokenAndGetInfo(token);

        // 중복 검사
        authService.verifyPhoneAuthInfo(phoneAuthInfo);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "아이디 찾기",
            description = "파이어베이스에서 추출한 토큰이 맞는지 검증하고, 토큰에 포함된 UID와 전화번호로 사용자의 ID를 찾아서 반환한다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 성공",
                    content = @Content(schema = @Schema(implementation = UserIdResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (헤더 형식 오류, 토큰 누락, 필수 정보 누락 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 만료, 폐기, 유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/id/find")
    public ResponseEntity<?> findUserId(
            @Parameter(name = "Authorization", description = "Bearer {Firebase_Token}", required = true)
            @RequestHeader("Authorization") String token) {
        //토큰 검증 및 유저 정보 추출
        PhoneAuthInfoDto phoneAuthInfoDto = firebaseService.verifyTokenAndGetInfo(token);

        // 유저 ID 찾기
        Member findMember = authService.findUserId(phoneAuthInfoDto);
        UserIdResponseDto response = UserIdResponseDto.builder().userId(findMember.getUserId()).build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "비밀번호 재설정 사용자 검증",
            description = "전달받은 Firebase ID 토큰의 유효성을 검증하고, 토큰 내 UID와 입력된 사용자 ID의 일치 여부를 확인한다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "인증 성공",
                    content = @Content(schema = @Schema(implementation = PasswordResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (헤더 형식 오류, 토큰 누락, 필수 정보 누락, 사용자 정보 불일치 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 만료, 폐기, 유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    }
    )
    @PostMapping("/password/verify")
    public ResponseEntity<?> verifyUserForPasswordReset(
            @Parameter(name = "Authorization", description = "Bearer {Firebase_Token}", required = true)
            @RequestHeader("Authorization") String token,
            @RequestBody PasswordRequestDto passwordRequestDto
    ) {
        //토큰 검증 및 유저 정보 추출
        PhoneAuthInfoDto phoneAuthInfoDto = firebaseService.verifyTokenAndGetInfo(token);
        // 유저 정보 검증
        Member member = authService.authenticateUserForPasswordReset(phoneAuthInfoDto, passwordRequestDto.getUserId());
        // 토큰 생성 및 발급
        String passwordResetToken = jwtTokenUtil.createPasswordRestToken(member);
        PasswordResponseDto response = PasswordResponseDto.builder().passwordResetToken(passwordResetToken).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "비밀번호 재설정", description = "비밀번호 재설정 토큰을 검증하고 사용자의 비밀번호를 변경한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공(응답 바디 X)"),
            @ApiResponse(responseCode = "401", description = "토큰의 유효성 검증 실패 혹은 토큰 만료",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetRequestDto passwordResetRequestDto) {
        //토큰 검증 및 사용자 추출
        Long id = jwtTokenUtil.validatePasswordResetToken(passwordResetRequestDto.getPasswordResetToken());
        //패스워드 변경
        authService.resetPassword(id, passwordResetRequestDto.getNewPassword());
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
