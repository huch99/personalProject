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
	
	@GetMapping
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
            @RequestParam(name = "cltrNm", required = false) String cltrNm,
            @RequestParam(name = "dpslMtdCd", required = false) String dpslMtdNm, // 엔티티 organization 필드와 매핑
            @RequestParam(name = "sido", required = false) String sido,
            @RequestParam(name = "sgk", required = false) String sgk,
            @RequestParam(name = "emd", required = false) String emd,
            @RequestParam(name = "goodsPriceFrom", required = false) String goodsPriceFrom,
            @RequestParam(name = "goodsPriceTo", required = false) String goodsPriceTo,
            @RequestParam(name = "openPriceFrom", required = false) String openPriceFrom, // 현재 DB 엔티티에 매핑되는 필드 없음 (향후 확장용)
            @RequestParam(name = "openPriceTo", required = false) String openPriceTo,   // 현재 DB 엔티티에 매핑되는 필드 없음 (향후 확장용)
            @RequestParam(name = "pbctBegnDtm", required = false) String pbctBegnDtm,   // String으로 받아서 서비스에서 파싱
            @RequestParam(name = "pbctClsDtm", required = false) String pbctClsDtm,    // String으로 받아서 서비스에서 파싱
            @RequestParam(name = "pageNo", defaultValue = "1") int pageNo,
            @RequestParam(name = "numOfRows", defaultValue = "10") int numOfRows) {

        log.info("Request for search tenders with cltrNm: {}, dpslMtdCd: {}, pageNo: {}, numOfRows: {}",
                cltrNm, dpslMtdNm, pageNo, numOfRows);
        try {
            PagedTenderResponse tenders = tenderService.searchTenders(
                    cltrNm, dpslMtdNm, sido, sgk, emd,
                    goodsPriceFrom, goodsPriceTo, openPriceFrom, openPriceTo,
                    pbctBegnDtm, pbctClsDtm,
                    pageNo, numOfRows
            );
            log.info("Successfully fetched search tenders. Total count: {}", tenders.getTotalCount());
            return ResponseEntity.ok(tenders);
        } catch (Exception e) {
            log.error("Error searching tenders: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
