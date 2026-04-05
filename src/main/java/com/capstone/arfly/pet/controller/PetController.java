package com.capstone.arfly.pet.controller;


import com.capstone.arfly.common.exception.ErrorResponse;
import com.capstone.arfly.pet.domain.Species;
import com.capstone.arfly.pet.dto.CreatePetRequest;
import com.capstone.arfly.pet.dto.PetDetailResponse;
import com.capstone.arfly.pet.dto.PetListResponse;
import com.capstone.arfly.pet.dto.UpdatePetRequest;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            @AuthenticationPrincipal UserDetails userDetails) {

        Long memberId = Long.parseLong(userDetails.getUsername());
        petService.createPet(memberId, request, file);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(
            summary = "품종 목록 조회",
            description = "선택한 종(DOG 또는 CAT)에 해당하는 품종 이름 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "품종 목록 조회 성공")
    })
    @GetMapping("/breeds")
    public ResponseEntity<List<String>> getBreeds(
            @Parameter(description = "동물 종 (DOG 또는 CAT)", required = true)
            @RequestParam(name = "species") Species species) {

        List<String> breedNames = petService.getBreedsBySpecies(species);

        return ResponseEntity.ok(breedNames);
    }

    @Operation(
            summary = "반려동물 정보 수정",
            description = "기존 반려동물의 정보를 수정합니다. 사진과 정보(JSON)를 함께 보냅니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "반려동물 수정 성공 (본문 없음)"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음 (내 반려동물이 아님)"),
            @ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없음")
    })
    @PostMapping(value = "/{petId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePet(
            @Parameter(description = "수정할 반려동물의 ID", required = true)
            @PathVariable Long petId,

            @Parameter(description = "반려동물 수정 정보 (application/json)", schema = @Schema(implementation = UpdatePetRequest.class))
            @RequestPart(value = "request") UpdatePetRequest request,

            @Parameter(description = "새로운 프로필 사진 파일 (없으면 기존 사진 유지)", schema = @Schema(type = "string", format = "binary"))
            @RequestPart(value = "file", required = false) MultipartFile petFile,

            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        Long memberId = Long.parseLong(userDetails.getUsername());

        petService.updatePet(memberId, petId, request, petFile);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "반려동물 상세 조회",
            description = "특정 반려동물의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음 (내 반려동물이 아님)"),
            @ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없음")
    })
    @GetMapping("/{petId}")
    public ResponseEntity<PetDetailResponse> getPetDetail(
            @Parameter(description = "조회할 반려동물의 ID", required = true)
            @PathVariable Long petId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        Long memberId = Long.parseLong(userDetails.getUsername());

        PetDetailResponse response = petService.getPetDetail(memberId, petId);

        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "내 반려동물 목록 조회",
            description = "내 계정에 등록된 모든 반려동물의 간단한 정보(ID, 이름, 사진) 목록을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    @GetMapping
    public ResponseEntity<PetListResponse> getPetList(
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails){

        Long memberId = Long.parseLong(userDetails.getUsername());
        PetListResponse response = petService.getPetList(memberId);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "반려동물 삭제",
            description = "반려동물 정보를 삭제합니다. 연결된 프로필 이미지는 자정에 S3에서 자동으로 삭제됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "반려동물 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (내 반려동물이 아님)"),
            @ApiResponse(responseCode = "404", description = "반려동물을 찾을 수 없음")
    })
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(
            @Parameter(description = "삭제할 반려동물 ID", required = true)
            @PathVariable Long petId,
            @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails) {

        Long memberId = Long.parseLong(userDetails.getUsername());
        petService.deletePet(memberId, petId);
        return ResponseEntity.noContent().build();
    }

}
