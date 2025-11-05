package com.bid.security;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bid.entity.User;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {

	private Long userId; // ✅ 사용자 ID 필드 (가장 중요)
	private String username;
	private String password;
	private Collection<? extends GrantedAuthority> authorities; // 사용자 권한 목록

	// 모든 필드를 포함하는 생성자
	public CustomUserDetails(Long userId, String username, String password,
			Collection<? extends GrantedAuthority> authorities) {
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.authorities = authorities;
	}

	// User 엔티티로부터 CustomUserDetails 객체를 생성하는 팩토리 메서드
	public static CustomUserDetails create(User user) {
		// User 엔티티의 userRoles를 GrantedAuthority 컬렉션으로 변환
		List<GrantedAuthority> authorities = user.getUserRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getRoleName())) // Role 엔티티의 getRoleName() 사용
				.collect(Collectors.toList());

		return new CustomUserDetails(user.getUserId(), // User 엔티티의 userId를 CustomUserDetails의 userId로 설정
				user.getUsername(), user.getPassword(), authorities);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true; // 계정 만료 여부 (true면 만료되지 않음)
	}

	@Override
	public boolean isAccountNonLocked() {
		return true; // 계정 잠금 여부 (true면 잠겨 있지 않음)
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true; // 자격 증명(비밀번호) 만료 여부 (true면 만료되지 않음)
	}

	@Override
	public boolean isEnabled() {
		return true; // 계정 활성화 여부 (true면 활성화됨)
	}

//	@Override
//	protected void doFilterInternal(HttpServletRequest request,
//	                                HttpServletResponse response,
//	                                FilterChain filterChain) throws ServletException, IOException {
//	    try {
//	        String jwt = getJwtFromRequest(request);
//
//	        if (jwt != null && JwtTokenProvider.validateToken(jwt)) {
//	            String username = jwtTokenProvider.getUsernameFromJwt(jwt); // 토큰에서 사용자 이름 추출
//
//	            // ✅ CustomUserDetailsService를 통해 UserDetails(CustomUserDetails) 로드
//	            // Huch님의 CustomUserDetailsService는 loadUserByUsername으로 CustomUserDetails를 반환해야 합니다.
//	            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
//
//	            // ✅ 여기서 UsernamePasswordAuthenticationToken의 첫 번째 인자 (Principal)로 CustomUserDetails 객체를 전달합니다.
//	            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//	                    userDetails, // ✅ 바로 이 부분이 CustomUserDetails 객체여야 합니다.
//	                    null,
//	                    userDetails.getAuthorities());
//	            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//
//	            // SecurityContextHolder에 인증 정보 저장
//	            SecurityContextHolder.getContext().setAuthentication(authentication);
//	        }
//	    } catch (Exception ex) {
//	        log.error("Could not set user authentication in security context", ex);
//	        // 여기서 발생한 예외는 HTTP 응답으로 연결되지 않고 필터 체인만 중단시킵니다.
//	    }
//
//	    filterChain.doFilter(request, response);
//	}
}
