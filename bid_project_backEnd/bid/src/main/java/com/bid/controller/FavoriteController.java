package com.bid.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bid.dto.response.TenderResponseDTO;
import com.bid.entity.Tender;
import com.bid.security.CustomUserDetails;
import com.bid.service.FavoriteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
public class FavoriteController {

	private final FavoriteService favoriteService;

	// 현재 인증된 사용자 ID 가져오기
	private Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		log.info("getCurrentUserId() completed. Returning userId: {}", ((CustomUserDetails)authentication.getPrincipal()).getUserId());
		if (authentication != null) {
	         log.info("  Authentication is authenticated: {}", authentication.isAuthenticated()); // ✅ 이 로그 확인
	         log.info("  Authorities in Authentication: {}", authentication.getAuthorities()); // ✅ 이 로그 확인
	     }
		
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new IllegalStateException("인증된 사용자가 아닙니다. JWT 토큰을 확인해주세요.");
		}
		
		Object principal = authentication.getPrincipal();
		if (principal instanceof CustomUserDetails customUserDetails) { 
			Long userId = customUserDetails.getUserId();
			log.info("Successfully cast principal to CustomUserDetails. User ID: {}", userId);
			return customUserDetails.getUserId();
        } else if (principal instanceof UserDetails userDetails) {
            throw new IllegalStateException("Authentication principal is UserDetails, but not CustomUserDetails. Cannot get user ID safely. Principal username: " + userDetails.getUsername());
        } else {
            throw new IllegalStateException("Authentication principal이 UserDetails 타입이 아닙니다. (타입: " + principal.getClass().getName() + ", 값: " + principal.toString() + ")");
        }
		
	}

	// 즐겨찾기 추가
	@PostMapping("/{cltrMnmtNo}")
	public ResponseEntity<Map<String, Boolean>> addFavorite(@PathVariable("cltrMnmtNo") String cltrMnmtNo) {
		log.info("FavoriteController - addFavorite entered for cltrMnmtNo: {}", cltrMnmtNo); // ✅ 로그
        try {
            Long userId = getCurrentUserId(); // 이 로그 확인
            log.info("FavoriteController - userId obtained: {}", userId); // ✅ 로그
            boolean added = favoriteService.addFavorite(userId, cltrMnmtNo);
            log.info("FavoriteController - favoriteService.addFavorite returned: {}", added); // ✅ 로그
            Map<String, Boolean> response = new HashMap<>();
            response.put("success", added);
            response.put("isFavorite", true);
            return ResponseEntity.status(added ? HttpStatus.CREATED : HttpStatus.CONFLICT).body(response);
        } catch (Exception e) { // ✅ 여기서 모든 예외를 잡아서 상세 로깅
            log.error("FavoriteController - addFavorite failed for cltrMnmtNo: {}. Error: {}", cltrMnmtNo, e.getMessage(), e);
            // 명확한 오류 반환을 위해 HttpStatus를 400 또는 500으로 변경 고려
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "isFavorite", false));
        }
	}

	// 즐겨찾기 삭제
	@DeleteMapping("/{cltrMnmtNo}")
	public ResponseEntity<Map<String, Boolean>> removeFavorite(@PathVariable("cltrMnmtNo") String cltrMnmtNo) {
		Long userId = getCurrentUserId();
		boolean removed = favoriteService.removeFavorite(userId, cltrMnmtNo);
		Map<String, Boolean> response = new HashMap<>();
		response.put("success", removed);
		response.put("isFavorite", false);
		return ResponseEntity.ok(response);
	}

	// 특정 입찰 공고의 즐겨찾기 여부 확인
	@GetMapping("/check/{cltrMnmtNo}")
	public ResponseEntity<Map<String, Boolean>> checkFavoriteStatus(@PathVariable("cltrMnmtNo") String cltrMnmtNo) {
		Long userId = getCurrentUserId();
		boolean isFavorite = favoriteService.isFavorite(userId, cltrMnmtNo);
		Map<String, Boolean> response = new HashMap<>();
		response.put("isFavorite", isFavorite);
		return ResponseEntity.ok(response);
	}

	// 현재 사용자의 즐겨찾기 목록 조회
	@GetMapping
	public ResponseEntity<List<TenderResponseDTO>> getMyFavorites() {
		Long userId = getCurrentUserId();
		List<TenderResponseDTO> favoriteTenders = favoriteService.getFavoriteTendersByUserId(userId);
		return ResponseEntity.ok(favoriteTenders);
	}
}
