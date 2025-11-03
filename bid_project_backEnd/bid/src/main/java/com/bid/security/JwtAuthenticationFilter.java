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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	// JwtTokenProvider는 JWT 토큰을 생성하고 검증하는 유틸리티
    private final JwtTokenProvider jwtTokenProvider;
    // UserDetailsService는 사용자 정보를 로드하는 Spring Security 서비스 (아래에서 구현 예정)
    private final UserDetailsService userDetailsService;

    /**
     * 모든 HTTP 요청에 대해 필터링을 수행합니다.
     * JWT 토큰을 추출하고 유효성을 검사하여 Spring Security 컨텍스트에 인증 정보를 설정합니다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 요청 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);

            // 2. JWT 토큰 유효성 검사 및 사용자 인증 정보 설정
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromJWT(jwt); // 토큰에서 사용자 이름 추출

                // UserDetailsService를 통해 사용자 정보 로드 (User 엔티티 -> UserDetails 변환)
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                // Spring Security의 UsernamePasswordAuthenticationToken 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                // 요청에 대한 웹 인증 세부 정보 설정 (IP 주소, 세션 ID 등)
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 현재 SecurityContext에 인증 객체 설정
                // 이렇게 설정하면 Spring Security의 @PreAuthorize 등 어노테이션에서 이 정보를 사용할 수 있습니다.
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // 토큰 파싱 또는 인증 과정에서 발생한 예외를 로그
            log.error("Security Context에 사용자 인증을 설정할 수 없습니다.", ex);
            // 클라이언트에 특정 오류 응답을 주기 위해 response.sendError 등을 사용할 수도 있습니다.
            // 하지만 여기서는 예외를 잡아서 로그만 남기고 다음 필터로 진행합니다.
            // InvalidJwtAuthenticationEntryPoint에서 처리될 수 있도록 던지는 것도 고려해볼 수 있습니다.
        }

        // 다음 필터로 요청과 응답을 전달
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 추출하는 헬퍼 메서드
     * "Authorization: Bearer <JWT_TOKEN>" 형식에서 토큰 부분을 파싱합니다.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization"); // Authorization 헤더 가져오기
        // Bearer 접두사로 시작하는지 확인하고 토큰 부분만 반환
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " (7자) 이후의 문자열
        }
        return null;
    }
}
