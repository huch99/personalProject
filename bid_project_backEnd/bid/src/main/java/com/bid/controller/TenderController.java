package com.bid.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bid.dto.response.PagedTenderResponse;
import com.bid.dto.response.TenderResponseDTO;
import com.bid.service.TenderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/tenders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
public class TenderController {

	private final TenderService tenderService;
	
	/**
     * 모든 입찰 정보를 조회하는 API
     * GET /api/tenders
     */
    @GetMapping
    public ResponseEntity<List<TenderResponseDTO>> getAllTenders() {
        List<TenderResponseDTO> tenders = tenderService.getAllTenders();
        return ResponseEntity.ok(tenders); // HTTP 200 OK 응답과 함께 입찰 목록 반환
    }
    
    /**
     * 특정 입찰 정보를 조회하는 API (프론트 상세 페이지용)
     * GET /api/tenders/{tenderId}
     */
    @GetMapping("/{tenderId}")
    public ResponseEntity<TenderResponseDTO> getTenderById(@PathVariable("tenderId") Long tenderId) {
//    	TenderResponseDTO tender = tenderService.getTenderByTenderId(tenderId);
//        return ResponseEntity.ok(tender); // HTTP 200 OK 응답과 함께 단일 입찰 정보 반환
    	throw new RuntimeException("온비드 공공 API는 현재 상세 ID 조회를 직접 지원하지 않습니다. 로직 수정 필요.");
    }
    
    @GetMapping("/search")
    public ResponseEntity<PagedTenderResponse> searchTenders(
            @RequestParam(name = "cltrNm", required = false) String cltrNm,             // 물건명
            @RequestParam(name = "dpslMtdCd",required = false) String dpslMtdCd,          // 처분방식코드 (0001 매각, 0002 임대)
            @RequestParam(name = "sido",required = false) String sido,               // 물건소재지 (시도)
            @RequestParam(name = "sgk",required = false) String sgk,                // 물건소재지 (시군구)
            @RequestParam(name = "emd",required = false) String emd,                // 물건소재지 (읍면동)
            @RequestParam(name = "goodsPriceFrom",required = false) String goodsPriceFrom,     // 감정가하한
            @RequestParam(name = "goodsPriceTo",required = false) String goodsPriceTo,       // 감정가상한
            @RequestParam(name = "openPriceFrom",required = false) String openPriceFrom,      // 최저입찰가하한
            @RequestParam(name = "openPriceTo",required = false) String openPriceTo,        // 최저입찰가상한
            @RequestParam(name = "pbctBegnDtm",required = false) String pbctBegnDtm,        // 입찰일자 From (YYYYMMDD)
            @RequestParam(name = "pbctClsDtm",required = false) String pbctClsDtm,         // 입찰일자 To (YYYYMMDD)
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,              // 페이지 번호
            @RequestParam(name = "numOfRows", defaultValue = "100") int numOfRows          // 페이지당 데이터 개수 (충분히 크게)
    ) {
        // 서비스 메서드 호출
    	PagedTenderResponse pagedResponse  = tenderService.searchTenders(
                cltrNm, dpslMtdCd, sido, sgk, emd,
                goodsPriceFrom, goodsPriceTo, openPriceFrom, openPriceTo,
                pbctBegnDtm, pbctClsDtm,
                pageNo, numOfRows
        );

        if (pagedResponse.getTenders().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(pagedResponse );
    }
}
