package com.bid.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bid.entity.User;
import com.bid.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service // Spring 빈으로 등록
@RequiredArgsConstructor // final 필드를 초기화하는 생성자 자동 생성
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	@Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

		return CustomUserDetails.create(user);
	}


}
