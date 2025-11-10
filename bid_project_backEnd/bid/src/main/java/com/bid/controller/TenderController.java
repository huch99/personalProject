package com.bid.controller;

import java.util.List;
import java.util.NoSuchElementException;

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
	@GetMapping // /api/tenders
    public ResponseEntity<PagedTenderResponse> getAllTenders(
            @RequestParam(name ="pageNo", defaultValue = "1") int pageNo) {    // ✅ numOfRows 파라미터 추가 (기본값 10)
		try {
            // ✅ numOfRows는 고정값 10을 서비스로 전달
            PagedTenderResponse tenders = tenderService.getAllTenders(pageNo, 10);
            log.info("Successfully fetched all tenders. Total count: {}", tenders.getTotalCount());
            return ResponseEntity.ok(tenders);
        } catch (Exception e) {
            log.error("Error fetching all tenders: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping("/{cltrMnmtNo}")
    public ResponseEntity<TenderResponseDTO> getTenderByTenderId(@PathVariable("cltrMnmtNo") String cltrMnmtNo) {
    	 log.info("✅ Controller: getTenderDetail 요청 시작, CLTR_MNMT_NO: {}", cltrMnmtNo);
         try {
             // 서비스 메서드 호출
             TenderResponseDTO tenderDetail = tenderService.getTenderDetail(cltrMnmtNo); // ✅ cltrMnmtNo 전달
             log.info("✅ Controller: Tender detail fetched successfully for CLTR_MNMT_NO: {}", cltrMnmtNo);
             return ResponseEntity.ok(tenderDetail);
         } catch (NoSuchElementException e) {
             log.warn("Controller: Tender with CLTR_MNMT_NO {} not found: {}", cltrMnmtNo, e.getMessage());
             return ResponseEntity.notFound().build();
         } catch (Exception e) {
             log.error("Controller: Error fetching tender detail for CLTR_MNMT_NO {}: {}", cltrMnmtNo, e.getMessage(), e);
             return ResponseEntity.status(500).build();
         }
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
