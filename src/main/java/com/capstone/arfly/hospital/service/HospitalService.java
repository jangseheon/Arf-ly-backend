package com.capstone.arfly.hospital.service;

import com.capstone.arfly.common.exception.BusinessException;
import com.capstone.arfly.common.exception.ErrorCode;
import com.capstone.arfly.hospital.dto.HospitalDetailResponse;
import com.capstone.arfly.hospital.dto.HospitalListResponse;
import com.capstone.arfly.member.domain.Member;
import com.capstone.arfly.member.repository.MemberRepository;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.StatusCode;
import com.google.maps.places.v1.*;
import com.google.type.LatLng;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class HospitalService {

    private final MemberRepository memberRepository;

    private final PlacesClient placesClient;

    // 주변 병원 리스트 가져오기
    public List<HospitalListResponse> getHospitalList(Long userId){
        Member member = memberRepository.findById(userId).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_EXISTS));
        Double latitude = member.getLatitude();
        Double longitude = member.getLongitude();

        List<HospitalListResponse> hospitalList = new ArrayList<>();

        try {
            SearchNearbyResponse response = getMapResponse(latitude,longitude);

            for (Place place : response.getPlacesList()) {
                String imageUrl = null;

                // 이미지 조회를 위한 url 생성(구글 맵 api에서 불러온 사진이 없으면 null)
                if (!place.getPhotosList().isEmpty()) {
                    String photoName = place.getPhotosList().get(0).getName();
                    imageUrl = "/api/v1/hospitals/photo?name=" + photoName;
                }

                HospitalListResponse hospitalDto = HospitalListResponse.builder()
                        .id(place.getId())
                        .hospitalName(place.getDisplayName().getText())
                        .latitude(place.getLocation().getLatitude())
                        .longitude(place.getLocation().getLongitude())
                        .roadAddress(place.getShortFormattedAddress())
                        .opened(place.hasRegularOpeningHours() && place.getRegularOpeningHours().getOpenNow())
                        .imageUrl(imageUrl)
                        .build();

                hospitalList.add(hospitalDto);
            }
        } catch (Exception e) {
            log.error("데이터 조회 중 에러 발생: ", e);
            throw new BusinessException(ErrorCode.GOOGLE_MAP_ERROR);
        }

        return hospitalList;
    }

    // 장소 사진 함수
    public byte[] getHospitalPhoto(Long userId, String photoName, Integer maxHeight) {

        memberRepository.findById(userId).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_EXISTS));

        try {
            PhotoMedia photo = getPhotoResponse(photoName, maxHeight);

            String uri = photo.getPhotoUri();

            RestTemplate restTemplate = new RestTemplate();

            return restTemplate.getForObject(uri, byte[].class);

        } catch (HttpClientErrorException.NotFound e) {
            // 구글 맵에 해당 장소의 사진이 없는 경우
            log.info("해당 병원에 사진이 없습니다.");
            return null;
        } catch (Exception e){
            log.error("구글 사진 로드 중 오류 발생. photoName: {}, 원인: {}", photoName, e.getMessage());
            throw new BusinessException(ErrorCode.MAP_PHOTO_ERROR);
        }
    }

    // 병원 상세정보 가져오기
    public HospitalDetailResponse getHospitalDetail(Long userId, String placesId){
        memberRepository.findById(userId).orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_EXISTS));

        try{
            Place response = getPlaceDetailResponse(placesId);

            List<String> imageUrls = new ArrayList<>();

            // 사진 없으면 response 안함, 최대 5개
            if (response.getPhotosCount() > 0) {
                int photoCount = Math.min(5, response.getPhotosCount());

                for(int i = 0; i < photoCount; i++) {
                    String photoName = response.getPhotos(i).getName();
                    imageUrls.add("/api/v1/hospitals/photo?name=" + photoName);
                }
            }

            // 구글맵에 영업시간이 없으면 빈 리스트
            List<String> operatingHours = response.hasRegularOpeningHours()
                    ? response.getRegularOpeningHours().getWeekdayDescriptionsList()
                    : Collections.emptyList();

            HospitalDetailResponse hospitalDto = HospitalDetailResponse.builder()
                    .id(response.getId())
                    .hospitalName(response.getDisplayName().getText())
                    .roadAddress(response.getShortFormattedAddress())
                    .operating(operatingHours)
                    .opened(response.hasRegularOpeningHours() && response.getRegularOpeningHours().getOpenNow())
                    .imageUrl(imageUrls)
                    .build();

            return hospitalDto;
        }catch (ApiException e) {
            log.error("데이터 조회 중 에러 발생: ", e);

            // 잘못된 인자 or placesId 조회가 결과가 없을 때
            if (e.getStatusCode().getCode().equals(StatusCode.Code.INVALID_ARGUMENT) ||
                    e.getStatusCode().getCode().equals(StatusCode.Code.NOT_FOUND)) {
                throw new BusinessException(ErrorCode.INVALID_PLACES_ID); // 400 Error
            }

            throw new BusinessException(ErrorCode.GOOGLE_MAP_ERROR);
        }catch (Exception e) {
            // 그 외 런타임 에러 처리
            log.error("병원 상세 정보 처리 중 예상치 못한 에러: ", e);
            throw new BusinessException(ErrorCode.GOOGLE_MAP_ERROR);
        }
    }

    // 주변 병원 리스트(구글 api) 호출 함수
    public SearchNearbyResponse getMapResponse(Double latitude, Double longitude){

        // api(Nearby search) request 생성
        SearchNearbyRequest request = SearchNearbyRequest.newBuilder()
                .setLocationRestriction(
                        SearchNearbyRequest.LocationRestriction.newBuilder()
                                .setCircle(Circle.newBuilder()
                                        .setCenter(LatLng.newBuilder().setLatitude(latitude).setLongitude(longitude).build())
                                        .setRadius(2000.0)
                                        .build())
                                .build()
                )
                .addIncludedTypes("veterinary_care") // 동물병원 필터링
                .setMaxResultCount(10)
                .setLanguageCode("ko")
                .build();

        return placesClient.searchNearby(request);
    }

    public PhotoMedia getPhotoResponse(String photoName, Integer maxHeight) {
        GetPhotoMediaRequest request = GetPhotoMediaRequest.newBuilder()
                .setName(photoName)
                .setMaxHeightPx(maxHeight)
                .build();

        return placesClient.getPhotoMedia(request);
    }

    public Place getPlaceDetailResponse(String placeId) {

        GetPlaceRequest request = GetPlaceRequest.newBuilder()
                .setName("places/" + placeId)
                .setLanguageCode("ko")
                .build();

        return placesClient.getPlace(request);
    }

}
