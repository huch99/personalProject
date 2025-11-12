package com.bid.security;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
		List<GrantedAuthority> authorities;

		if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
			authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
		} else {
			authorities = user.getUserRoles().stream().map(role -> new SimpleGrantedAuthority(role.getRoleName()))
					.collect(Collectors.toList());
		}

		List<GrantedAuthority> finalAuthorities = authorities.stream().map(authority -> {
			String roleName = authority.getAuthority();
			if (!roleName.startsWith("ROLE_")) {
				return new SimpleGrantedAuthority("ROLE_" + roleName);
			}
			return authority;
		}).collect(Collectors.toList());

		return new CustomUserDetails(user.getUserId(), user.getUsername(), user.getPassword(), finalAuthorities);
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

}
