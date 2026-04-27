package com.capstone.arfly.member.controller;

import com.capstone.arfly.common.exception.ErrorResponse;
import com.capstone.arfly.member.dto.UpdateUserRequest;
import com.capstone.arfly.member.dto.UserIdCheckRequestDto;
import com.capstone.arfly.member.dto.UserIdCheckResponseDto;
import com.capstone.arfly.member.dto.UserNameCheckRequestDto;
import com.capstone.arfly.member.dto.UserNameCheckResponseDto;
import com.capstone.arfly.member.dto.UserProfileResponse;
import com.capstone.arfly.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;


    @Operation(
            summary = "닉네임 중복 확인",
            description = "전달받은 닉네임이 DB에 이미 존재하는지 확인하여 사용 가능 여부를 반환합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserNameCheckResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (닉네임 값이 비어있거나 공백인 경우)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })

    @PostMapping("/check-username")
    public ResponseEntity<?> checkUsernameAvailability(@RequestBody @Valid UserNameCheckRequestDto userNameCheckRequestDto) {
        UserNameCheckResponseDto response;
        if (memberService.isUsernameAvailable(userNameCheckRequestDto)) {
            response = UserNameCheckResponseDto.from(true);
        } else {
            response = UserNameCheckResponseDto.from(false);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "ID 중복 확인",
            description = "전달받은 ID가 DB에 이미 존재하는지 확인하여 사용 가능 여부를 반환한다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserIdCheckResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (ID 값이 비어있거나 공백인 경우, 유효성 검증에 실패한 경우)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })

    @PostMapping("/check-userId")
    public ResponseEntity<?> checkUserIdAvailability(@RequestBody @Valid UserIdCheckRequestDto userIdCheckRequestDto) {
        UserIdCheckResponseDto response;
        if (memberService.isIdAvailable(userIdCheckRequestDto)) {
            response = UserIdCheckResponseDto.from(true);
        } else {
            response = UserIdCheckResponseDto.from(false);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 프로필 상세 정보를 조회합니다. 헤더에 JWT Access 토큰이 필수입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 만료 혹은 유효하지 않은 토큰)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 회원")
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        Long memberId = Long.parseLong(userDetails.getUsername());
        UserProfileResponse response = memberService.getMyProfile(memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "내 프로필 수정",
            description = "프로필 정보(닉네임, 비밀번호, 위치, 주소, 알림설정)를 수정합니다. (multipart/form-data 형식)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "프로필 수정 성공 (본문 없음)"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @PostMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,

            @Parameter(description = "유저 프로필 수정 정보 (application/json)", schema = @Schema(implementation = UpdateUserRequest.class))
            @RequestPart(value = "request") UpdateUserRequest request) {

        Long memberId = Long.parseLong(userDetails.getUsername());
        memberService.updateMyProfile(memberId, request);
        return ResponseEntity.noContent().build();
    }


}
