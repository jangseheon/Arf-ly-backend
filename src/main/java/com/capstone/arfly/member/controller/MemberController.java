package com.capstone.arfly.member.controller;

import com.capstone.arfly.common.exception.ErrorResponse;
import com.capstone.arfly.member.dto.UserNameCheckRequestDto;
import com.capstone.arfly.member.dto.UserNameCheckResponseDto;
import com.capstone.arfly.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> checkUsernameAvailability(@RequestBody UserNameCheckRequestDto userNameCheckRequestDto) {
        UserNameCheckResponseDto response;
        if (memberService.isUsernameAvailable(userNameCheckRequestDto)) {
            response = UserNameCheckResponseDto.from(true);
        } else {
            response = UserNameCheckResponseDto.from(false);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
