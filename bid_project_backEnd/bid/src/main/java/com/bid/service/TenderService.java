package com.bid.service;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.bid.dto.OnbidItem;
import com.bid.dto.OnbidResponseDTO;
import com.bid.dto.response.TenderResponseDTO;
import com.bid.entity.Tender;
import com.bid.repository.TenderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TenderService {

	private final RestTemplate restTemplate;
	
	@Value("${onbid.api.base-url}")
	private String onbidApiBaseUrl;
	
	@Value("${onbid.api.service-key}")
	private String onbidApiServiceKey;
	
	 /**
     * 모든 입찰 정보를 온비드 공공 API에서 조회하여 DTO 리스트로 반환합니다.
     * Huch님의 프론트엔드 HomePage에 표시될 데이터를 제공합니다.
     */
    public List<TenderResponseDTO> getAllTenders() {
        // 1. 온비드 API 호출을 위한 URI 구성
        URI uri = UriComponentsBuilder.fromUriString(onbidApiBaseUrl)
                .path("/services") // API의 엔드포인트, 실제 API 문서에 따라 변경 필요 (e.g., /portal/bid/list)
                .queryParam("serviceKey", onbidApiServiceKey)
                .queryParam("pageNo", 1)    // 페이지 번호
                .queryParam("numOfRows", 10) // 한 페이지당 결과 수
                // TODO: 온비드 API 문서에 따라 추가적인 쿼리 파라미터 (예: 공고일, 검색어 등) 추가
                .encode() // URI 인코딩
                .build()
                .toUri();

        log.info("Calling Onbid API: {}", uri); // 호출할 URI 로깅

        try {
            // 2. RestTemplate을 사용하여 온비드 API 호출
            ResponseEntity<OnbidResponseDTO> responseEntity = restTemplate.getForEntity(uri, OnbidResponseDTO.class);
            OnbidResponseDTO onbidResponse = responseEntity.getBody();
            // 3. 응답 처리 및 데이터 추출
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                
                if (onbidResponse.getResponse() != null &&
                    onbidResponse.getResponse().getBody() != null &&
                    onbidResponse.getResponse().getBody().getItems() != null) {

                    List<OnbidItem> onbidItems = onbidResponse.getResponse().getBody().getItems();
                    
                    // 4. 온비드 API 응답 (OnbidItem)을 우리 서비스의 DTO (TenderResponseDto)로 변환
                    return onbidItems.stream()
                            .map(this::mapOnbidItemToTenderResponseDTO)
                            .collect(Collectors.toList());
                } else {
                    log.warn("Onbid API 응답에 items가 없거나 구조가 예상과 다릅니다: {}", onbidResponse);
                    return Collections.emptyList();
                }
            } else {
                String errorMessage = String.format("Onbid API 호출 실패: %s - %s",
                                                    responseEntity.getStatusCode(),
                                                    responseEntity.getBody() != null ? onbidResponse.getResponse().getHeader().getResultMsg() : "No response body");
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } catch (Exception e) {
            log.error("Onbid API 호출 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("온비드 입찰 정보를 불러오는데 실패했습니다.", e);
        }
    }

    /**
     * OnbidItem을 TenderResponseDto로 변환하는 매핑 로직
     * 실제 온비드 API 응답 필드와 TenderResponseDto 필드명을 정확히 매핑해야 합니다.
     */
    private TenderResponseDTO mapOnbidItemToTenderResponseDTO(OnbidItem onbidItem) {
        // 공고일/마감일이 String으로 오는 경우, 파싱 로직 필요
        LocalDateTime announcementDate = parseDateTime(onbidItem.getNtceDt());
        LocalDateTime deadline = parseDateTime(onbidItem.getClsgDt());

        return TenderResponseDTO.builder()
                // TODO: 실제 온비드 Item의 필드명과 TenderResponseDto의 필드명 매핑
                .tenderId(generateUniqueId(onbidItem.getBidNo())) // 고유 ID 생성 방식 고민 (여기서는 bidNo 기반으로 임시 생성)
                .tenderTitle(onbidItem.getBidNm())
                .organization(onbidItem.getOrgNm())
                .bidNumber(onbidItem.getBidNo())
                .announcementDate(announcementDate)
                .deadline(deadline)
                .build();
    }
    
    // String 형태의 날짜/시간을 LocalDateTime으로 파싱 (API 문서의 날짜 형식 확인 필요)
    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        // TODO: 온비드 API 문서에 명시된 정확한 날짜/시간 포맷으로 DateTimeFormatter 수정
        // 예: "yyyyMMddHHmmss", "yyyy-MM-dd HH:mm:ss" 등
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss"); // 임시 포맷
        try {
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패: {} (포맷: {})", dateTimeString, formatter.toString());
            // 다른 포맷 시도 또는 null 반환 또는 기본값 설정
            return null;
        }
    }

    // 온비드 bidNo는 문자열일 수 있으므로, 우리 서비스의 Long 타입 ID로 변환/생성 로직 필요
    // 여기서는 간단히 문자열의 해시값 또는 일부를 이용하는 예시 (실제 서비스에서는 더 견고한 전략 필요)
    private Long generateUniqueId(String bidNo) {
        if (bidNo == null) return null;
        try {
            // bidNo가 순수한 숫자 문자열이면 Long으로 파싱
            return Long.parseLong(bidNo);
        } catch (NumberFormatException e) {
            // bidNo가 숫자 이외의 문자를 포함한다면, 예를 들어 해시코드를 이용
            return (long) bidNo.hashCode(); // 단순 예시. 실제 고유성을 보장하려면 다른 방법 모색
        }
    }
}
