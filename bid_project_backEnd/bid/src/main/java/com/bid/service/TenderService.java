package com.bid.service;

import java.io.StringReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import com.bid.dto.OnbidItem;
import com.bid.dto.response.PagedTenderResponse;
import com.bid.dto.response.TenderResponseDTO;
import com.bid.entity.Tender;
import com.bid.repository.TenderRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TenderService {

	private final TenderRepository tenderRepository;

	@Value("${onbid.api.base-url}")
	private String onbidApiBaseUrl;

	@Value("${onbid.api.service-key}")
	private String onbidApiServiceKey;

	private static final DateTimeFormatter ONBID_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	public PagedTenderResponse getAllTenders(int pageNo, int numOfRows) {
		// 1. 온비드 API 호출을 위한 URI 구성

		Pageable pageable = PageRequest.of(pageNo - 1, numOfRows, Sort.by(Sort.Direction.DESC, "announcementDate"));

		Page<Tender> tenderPage = tenderRepository.findByActiveTrue(pageable);

		List<TenderResponseDTO> dtoList = tenderPage.getContent().stream().map(TenderResponseDTO::fromEntity)
				.collect(Collectors.toList());

		LocalDateTime now = LocalDateTime.now();
		dtoList.sort((dto1, dto2) -> {
			LocalDateTime ann1 = dto1.getAnnouncementDate();
			LocalDateTime ann2 = dto2.getAnnouncementDate();
			if (ann1 == null && ann2 == null)
				return 0;
			if (ann1 == null)
				return 1;
			if (ann2 == null)
				return -1;
			boolean isDto1Started = !ann1.isAfter(now);
			boolean isDto2Started = !ann2.isAfter(now);
			if (isDto1Started && !isDto2Started) {
				return -1;
			}
			if (!isDto1Started && isDto2Started) {
				return 1;
			}
			if (isDto1Started && isDto2Started) {
				return ann2.compareTo(ann1);
			} else {
				return ann1.compareTo(ann2);
			}
		});

		return PagedTenderResponse.builder().tenders(dtoList).totalCount((int) tenderPage.getTotalElements()) // Long을
																												// int로
																												// 캐스팅
				.pageNo(pageNo).numOfRows(numOfRows).build();
	}

	public TenderResponseDTO getTenderDetail(String cltrMnmtNo) {
		Tender tender = tenderRepository.findByCltrMnmtNo(cltrMnmtNo)
				.orElseThrow(() -> new NoSuchElementException("물건관리번호 " + cltrMnmtNo + "를 찾을 수 없습니다."));

		return TenderResponseDTO.fromEntity(tender);
	}

	public PagedTenderResponse searchTenders(
            String cltrNm, String dpslMtdCd, String sido, String sgk, String emd,
            String goodsPriceFrom, String goodsPriceTo, String openPriceFrom, String openPriceTo,
            String pbctBegnDtmStr, String pbctClsDtmStr, // ✅ String으로 받아서 파싱
            int pageNo, int numOfRows) {

        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(pageNo - 1, numOfRows, Sort.by(Sort.Direction.DESC, "announcementDate"));

        // ✅ 날짜 String을 LocalDateTime으로 파싱
        LocalDateTime pbctBegnDtm = parseDateTime(pbctBegnDtmStr);
        LocalDateTime pbctClsDtm = parseDateTime(pbctClsDtmStr);
        
        // ✅ goodsPriceFrom/To (감정가) String을 Long으로 파싱
        Long minGoodsPrice = null;
        Long maxGoodsPrice = null;
        try {
            if (goodsPriceFrom != null && !goodsPriceFrom.isEmpty()) minGoodsPrice = Long.parseLong(goodsPriceFrom);
            if (goodsPriceTo != null && !goodsPriceTo.isEmpty()) maxGoodsPrice = Long.parseLong(goodsPriceTo);
        } catch (NumberFormatException e) {
            log.warn("감정가 필드 파싱 오류: {} 또는 {}. 검색에서 제외합니다.", goodsPriceFrom, goodsPriceTo);
        }


        // ✅ TenderRepository의 searchTendersByCriteria 메서드 호출
        Page<Tender> tenderPage = tenderRepository.searchTendersByCriteria(
                cltrNm, dpslMtdCd, sido, sgk, emd,
                minGoodsPrice, maxGoodsPrice, 
                pbctBegnDtm, pbctClsDtm,
                pageable
        );

        List<TenderResponseDTO> dtoList = tenderPage.getContent().stream()
                .map(TenderResponseDTO::fromEntity)
                .collect(Collectors.toList());

        // ✅ getAllTenders와 동일한 메모리 정렬 적용
        LocalDateTime now = LocalDateTime.now();
        dtoList.sort((dto1, dto2) -> {
            LocalDateTime ann1 = dto1.getAnnouncementDate();
            LocalDateTime ann2 = dto2.getAnnouncementDate();
            if (ann1 == null && ann2 == null) return 0;
            if (ann1 == null) return 1;
            if (ann2 == null) return -1;
            boolean isDto1Started = ann1.isBefore(now) || ann1.isEqual(now); // ann1 <= now
            boolean isDto2Started = ann2.isBefore(now) || ann2.isEqual(now); // ann2 <= now
            if (isDto1Started && !isDto2Started) { return -1; }
            if (!isDto1Started && isDto2Started) { return 1; }
            if (isDto1Started && isDto2Started) { return ann2.compareTo(ann1); }
            else { return ann1.compareTo(ann2); }
        });


        long endTime = System.currentTimeMillis();
        log.info("searchTenders from DB finished in {}ms. Total elements: {}", (endTime - startTime), tenderPage.getTotalElements());

        return PagedTenderResponse.builder()
                .tenders(dtoList)
                .totalCount((int) tenderPage.getTotalElements())
                .pageNo(pageNo)
                .numOfRows(numOfRows)
                .build();
    }
	
	// ✅ 날짜/시간 파싱 헬퍼 메서드
    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        try {
            // 온비드 API 날짜 포맷 (예: yyyyMMddHHmmss)으로 파싱
            // searchTenders는 DTO가 아니라 프론트에서 받은 yyyy-MM-dd HH:mm:ss 형태일 수 있습니다.
            // 여기서는 ISO_LOCAL_DATE_TIME 패턴 (yyyy-MM-ddTHH:mm:ss) 또는 yyyy-MM-dd HH:mm:ss를 가정합니다.
            // 프론트엔드에서 어떤 형식으로 날짜를 보내는지 확인 후 해당 포맷터 사용해야 합니다.
            if (dateTimeString.length() == 19 && dateTimeString.contains("T")) { // yyyy-MM-ddTHH:mm:ss
                return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else if (dateTimeString.length() == 19 && dateTimeString.contains(" ")) { // yyyy-MM-dd HH:mm:ss
                return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else if (dateTimeString.length() == 14) { // yyyyMMddHHmmss (온비드 API 포맷)
                return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            }
            log.warn("Unsupported date-time format for search: {}", dateTimeString);
            return null;
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date-time string in search: '{}'. Error: {}", dateTimeString, e.getMessage());
            return null;
        }
    }

}
