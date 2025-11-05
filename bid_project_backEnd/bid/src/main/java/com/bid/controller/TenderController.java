package com.bid.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bid.dto.response.TenderResponseDTO;
import com.bid.service.TenderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tenders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
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
}
