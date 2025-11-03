package com.bid.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bid.dto.request.SignupRequestDTO;
import com.bid.dto.response.LoginResponseDTO;
import com.bid.entity.User;
import com.bid.repository.UserRepository;
import com.bid.security.JwtTokenProvider;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder; // 비밀번호 암호화
	private final AuthenticationManager authenticationManager; // Spring Security 인증 관리자
	private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 관리

	/**
	 * 회원가입 로직
	 * 
	 * @param signupRequestDto 회원가입 요청 데이터 (username, password, email)
	 * @return 새로 생성된 User 엔티티
	 * @throws RuntimeException 사용자명/이메일 중복 시 예외 발생
	 */
	@Transactional // DB 변경이 일어나는 작업은 트랜잭션으로 묶기
	public User registerUser(SignupRequestDTO signupRequestDTO) {
		// 1. 사용자명 중복 확인
		if (userRepository.existsByUsername(signupRequestDTO.getUsername())) {
			throw new RuntimeException("이미 존재하는 사용자명입니다.");
		}
		// 2. 이메일 중복 확인 (선택 사항: 이메일도 유니크하게 관리하는 경우)
		if (signupRequestDTO.getEmail() != null && userRepository.existsByEmail(signupRequestDTO.getEmail())) {
			throw new RuntimeException("이미 가입된 이메일 주소입니다.");
		}

		// 3. User 엔티티 생성 및 비밀번호 암호화
		User newUser = User.builder().username(signupRequestDTO.getUsername())
				.password(passwordEncoder.encode(signupRequestDTO.getPassword())) // 비밀번호 암호화
				.email(signupRequestDTO.getEmail())
				// TODO: (선택 사항) Role 설정 (예: `User` 엔티티에 직접 역할 컬럼이 있다면 설정)
				// .roles(Collections.singleton(new Role("ROLE_USER"))) // Role 엔티티가 있다면 이렇게
				.build();

		// 4. DB에 저장
		return userRepository.save(newUser);
	}

	/**
	 * 로그인 로직
	 * 
	 * @param loginRequestDTO 로그인 요청 데이터 (username, password)
	 * @return 로그인 성공 시 JWT Access Token 및 사용자 정보 (LoginResponseDto)
	 */
	@Transactional // 트랜잭션으로 묶기 (토큰 생성 과정에 오류 대비)
	public LoginResponseDTO authenticateUser(LoginResponseDTO loginResponseDTO) {
		// 1. AuthenticationManager를 통해 사용자 인증 시도
		// 사용자명과 비밀번호를 기반으로 UsernamePasswordAuthenticationToken 생성
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
				loginResponseDTO.getUsername(), loginResponseDTO.getPassword()));

		// 2. 인증 성공 시 SecurityContextHolder에 인증 객체 저장 (선택 사항, 그러나 HTTP 요청이 진행되는 동안 컨텍스트에
		// 유지)
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// 3. 인증된 사용자 정보를 바탕으로 JWT 토큰 생성
		String jwt = jwtTokenProvider.generateToken(authentication);

		// 4. JWT 토큰과 사용자 정보를 포함한 응답 DTO 반환
		// 실제 유저의 상세 정보는 UserDetails에서 가져오거나, User 엔티티에서 필요한 정보만 필터링하여 반환
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		User user = userRepository.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userDetails.getUsername()));

		return LoginResponseDTO.builder().accessToken(jwt).username(user.getUsername())
				// TODO: 필요한 다른 사용자 정보 (예: 이메일, 역할 등) 추가
				.userId(user.getUserId()).message("로그인 성공").build();
	}
}
