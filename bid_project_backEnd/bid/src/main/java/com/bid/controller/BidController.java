package com.bid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bid.dto.request.BidRequestDTO;
import com.bid.dto.response.BidResponseDTO;
import com.bid.service.BidService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
@Slf4j
public class BidController {

	private final BidService bidService;
	
	@PostMapping
	public ResponseEntity<BidResponseDTO> placeBid(@Valid @RequestBody BidRequestDTO bidRequestDTO) {
		try {
			log.info("입찰 요청 수신 : tenderId={}, bidPrice={}", bidRequestDTO.getTenderId(),bidRequestDTO.getBidPrice());
			
			BidResponseDTO response = bidService.placeBid(bidRequestDTO);
			return ResponseEntity.status(HttpStatus.CREATED).body(response);
		} catch (IllegalArgumentException  e) {
			log.error("입찰 실패 (잘못된 요청) : {}",e.getMessage());
			return ResponseEntity.badRequest().body(BidResponseDTO.builder().message(e.getMessage()).build());
		} catch (Exception e) {
			log.error("입찰 처리 중 서버 오류 발생 : {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(BidResponseDTO.builder().message("입찰 처리 중 오류가 발생 했습니다.").build());
		}
	}
}
