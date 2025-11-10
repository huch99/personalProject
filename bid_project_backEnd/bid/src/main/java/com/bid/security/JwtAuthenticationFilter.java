package com.bid.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // UserDetailsService 인터페이스 사용
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	// JwtTokenProvider는 JWT 토큰을 생성하고 검증하는 유틸리티
	private final JwtTokenProvider jwtTokenProvider;
	// UserDetailsService는 사용자 정보를 로드하는 Spring Security 서비스 (아래에서 구현 예정)
	private final UserDetailsService userDetailsService;
	
	private final CustomUserDetailsService customUserDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String jwt = getJwtFromRequest(request);
			log.info("Request URL: {}", request.getRequestURI()); // ✅ 요청 URL 로그
			log.info("Extracted JWT: {}",
					(jwt != null ? jwt.substring(0, Math.min(jwt.length(), 30)) + "..." : "No JWT")); // ✅ JWT 추출 여부 로그

			if (jwt != null && jwtTokenProvider.validateToken(jwt)) {
				String username = jwtTokenProvider.getUsernameFromJWT(jwt); // 토큰에서 사용자 이름 추출
				UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

				// ✅ 이 부분이 CustomUserDetails 인스턴스인지 확인하는 로그
				if (userDetails instanceof CustomUserDetails) {
					log.info("User {} (ID: {}) authenticated via JWT.", username,
							((CustomUserDetails) userDetails).getUserId());
				} else {
					log.warn("UserDetails is not CustomUserDetails for user {}. Type: {}", username,
							userDetails.getClass().getName());
				}

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.info("User {} (ID: {}) successfully set in SecurityContext.", username,
						userDetails instanceof CustomUserDetails ? ((CustomUserDetails) userDetails).getUserId()
								: "N/A"); // ✅ SecurityContext에 저장 완료 로그
			} else if (jwt != null) {
				log.warn("JWT provided but invalid or expired for URL: {}", request.getRequestURI()); // ✅ 토큰은 있지만 유효하지
																										// 않은 경우
			} else {
				log.info("No JWT found in request for URL: {}", request.getRequestURI()); // ✅ 토큰이 없는 경우 (permitAll()
																							// 경로용)
			}
		} catch (Exception ex) {
			log.error("Authentication failed for URL: {}. Error: {}", request.getRequestURI(), ex.getMessage(), ex); // ✅
																														// 필터
																														// 내
																														// 예외
																														// 발생
																														// 시
																														// 로그
		}

		filterChain.doFilter(request, response);
	}

	private String getJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization"); // Authorization 헤더 가져오기
		// Bearer 접두사로 시작하는지 확인하고 토큰 부분만 반환
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7); // "Bearer " (7자) 이후의 문자열
		}
		return null;
	}
}
