package com.bid.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bid.dto.request.FaqRequestDTO;
import com.bid.dto.response.FaqResponseDTO;
import com.bid.security.CustomUserDetails;
import com.bid.service.FaqService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/faq")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class FaqController {

	private final FaqService faqService;

	private Long getCurrentUserId(Authentication authentication) {
		if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
			CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
			return userDetails.getUserId(); // ✅ Long 타입의 userId를 정확히 반환합니다.
		}

		if (authentication != null) {
			log.warn("Authentication principal type: {}. Expected CustomUserDetails.",
					authentication.getPrincipal().getClass().getName());
		} else {
			log.warn("Authentication object is null.");
		}
		return null; // 인증되지 않은 사용자
	}

	// FAQ 전체 목록 조회 (누구나 접근 가능)
	@GetMapping
	public ResponseEntity<List<FaqResponseDTO>> getAllFaqs() {
		List<FaqResponseDTO> faqs = faqService.getAllFaqs();
		return ResponseEntity.ok(faqs);
	}

	
	@GetMapping("/{faqId}")
    public ResponseEntity<FaqResponseDTO> getFaqById(@PathVariable("faqId") Long faqId) {
        try {
            FaqResponseDTO faq = faqService.getFaqById(faqId);
            return ResponseEntity.ok(faq);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
	
	// FAQ 생성 (로그인한 사용자만 가능)
	@PostMapping
	public ResponseEntity<FaqResponseDTO> createFaq(@Valid @RequestBody FaqRequestDTO requestDTO,
			Authentication authentication) {
		// 현재 인증된 사용자의 ID를 가져와서 작성자로 저장
		Long userId = getCurrentUserId(authentication);
		if (userId == null) {
			log.warn("인증되지 않은 사용자가 FAQ 생성을 시도했습니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 Unauthorized
		}

		try {
			FaqResponseDTO createdFaq = faqService.createFaq(requestDTO, userId);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdFaq); // 201 Created
		} catch (EntityNotFoundException e) {
			log.error("FAQ 생성 중 작성자를 찾을 수 없습니다: {}", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400 Bad Request
		}
	}

	// FAQ 수정 (로그인한 사용자 중 작성자만 가능)
	@PutMapping("/{faqId}")
	public ResponseEntity<FaqResponseDTO> updateFaq(@PathVariable("faqId") Long faqId,
			@Valid @RequestBody FaqRequestDTO requestDTO, Authentication authentication) {
		Long userId = getCurrentUserId(authentication);
		if (userId == null) {
			log.warn("인증되지 않은 사용자가 FAQ 수정을 시도했습니다. ID: {}", faqId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			FaqResponseDTO updatedFaq = faqService.updateFaq(faqId, requestDTO, userId);
			return ResponseEntity.ok(updatedFaq);
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (IllegalArgumentException e) {
			log.warn("FAQ 수정 권한 없음. FAQ ID: {}, User ID: {}. {}", faqId, userId, e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
		}
	}

	// FAQ 삭제 (로그인한 사용자 중 작성자만 가능)
	@DeleteMapping("/{faqId}")
	public ResponseEntity<Void> deleteFaq(@PathVariable("faqId") Long faqId, Authentication authentication) {
		Long userId = getCurrentUserId(authentication);
		if (userId == null) {
			log.warn("인증되지 않은 사용자가 FAQ 삭제를 시도했습니다. ID: {}", faqId);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		try {
			faqService.deleteFaq(faqId, userId);
			return ResponseEntity.noContent().build(); // 204 No Content
		} catch (EntityNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (IllegalArgumentException e) {
			log.warn("FAQ 삭제 권한 없음. FAQ ID: {}, User ID: {}. {}", faqId, userId, e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
		}
	}
}
