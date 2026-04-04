package com.capstone.arfly.pet.controller;


import com.capstone.arfly.common.exception.ErrorResponse;
import com.capstone.arfly.pet.dto.CreatePetRequest;
import com.capstone.arfly.pet.repository.PetRepository;
import com.capstone.arfly.pet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "pet", description = "반려동물 API")
@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @Operation(
            summary = "반려동물 등록",
            description = "새로운 반려동물의 정보(JSON)와 프로필 사진(File)을 함께 등록합니다. 헤더에 JWT Access 토큰이 필수입니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "반려동물 등록 성공 (바디 데이터 없음)"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (JSON 형식 오류, 필수 값 누락, 파일 용량 초과 등)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (토큰 만료 혹은 유효하지 않은 토큰)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 회원 또는 존재하지 않는 품종",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createPet(
            @Parameter(schema = @Schema(implementation = CreatePetRequest.class))
            @RequestPart(value = "request") CreatePetRequest request,
            @Parameter(schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value="file", required = false) MultipartFile file,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {

        Long memberId = Long.parseLong(user.getUsername());
        petService.createPet(memberId, request, file);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
