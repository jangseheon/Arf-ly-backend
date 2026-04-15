package com.capstone.arfly.hospital.controller;

import com.capstone.arfly.common.exception.ErrorResponse;
import com.capstone.arfly.hospital.dto.HospitalListResponse;
import com.capstone.arfly.hospital.service.HospitalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Hospital", description = "지도 관련 API")
@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
@Validated
public class HospitalController {

    private final HospitalService hospitalService;

    @Operation(summary = "지도 리스트 조회", description = "사용자가 설정한 위치의 주변 10개의 동물병원을 가져온다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "병원 리스트 가져오기 성공"),
            @ApiResponse(responseCode = "500", description = "구글 api 문제",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 회원",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/maps")
    public ResponseEntity<List<HospitalListResponse>> getHospitals(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        Long userId = Long.parseLong(userDetails.getUsername());

        return new ResponseEntity<>(hospitalService.getHospitalList(userId),HttpStatus.OK);
    }

    @Operation(summary = "장소 사진 조회", description = "사진을 불러온다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "병원 사진 조회 성공",
                    content = @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary"))
            ),
            @ApiResponse(responseCode = "500", description = "구글 api 문제",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "파라미터 에러(사진 이름 or 크기)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 회원",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/maps/photo")
    public ResponseEntity<byte[]> getHospitalPhoto(
            @RequestParam @NotBlank(message = "사진 이름은 필수입니다.")
                    @Parameter(description = "imageUrl", example = "/api/v1/hospitals/photo?name=places/ChIJNx.../photos/AUQs...")
            String photoName,
            @RequestParam(required = false, defaultValue = "400")
            @Parameter(description = "사진 최대 높이입니다. 200~1000 범위의 값으로 제한되어 있으며 변경하고 싶으시면 말씀해주세요, 기본값은 400입니다.", example = "300")
            @Min(value = 200, message = "최소 높이는 200 입니다.")
            @Max(value = 1000, message = "최대 높이는 1000 입니다.")
            Integer maxHeight,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        Long userId = Long.parseLong(userDetails.getUsername());

        byte[] imageBytes = hospitalService.getHospitalPhoto(userId, photoName, maxHeight);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageBytes);
    }
}
