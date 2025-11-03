package com.bid.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bid.dto.request.SignupRequestDTO;
import com.bid.dto.response.LoginResponseDTO;
import com.bid.service.LoginService;

@RestController // RESTful API 컨트롤러
@RequestMapping("/api/login") // 기본 URL 경로
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

	private final LoginService loginService;

	/**
	 * 회원가입 엔드포인트
	 * 
	 * @param signupRequestDTO 회원가입 요청 데이터
	 * @return 성공 메시지 또는 오류 응답
	 */
	@PostMapping("/signup")
	public ResponseEntity<String> registerUser(@RequestBody SignupRequestDTO signupRequestDTO) {
		log.info("회원가입 요청: {}", signupRequestDTO.getUsername());
		try {
			loginService.registerUser(signupRequestDTO);
			return new ResponseEntity<>("회원가입이 성공적으로 완료되었습니다.", HttpStatus.CREATED);
		} catch (RuntimeException e) {
			log.error("회원가입 실패: {}", e.getMessage());
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * 로그인 엔드포인트
	 * 
	 * @param loginRequestDTO 로그인 요청 데이터
	 * @return 로그인 성공 시 JWT 토큰 및 사용자 정보 (LoginResponseDTO)
	 */
	@PostMapping
	public ResponseEntity<LoginResponseDTO> authenticateUser(@RequestBody LoginResponseDTO loginRequestDTO) {
		log.info("로그인 요청: {}", loginRequestDTO.getUsername());
		try {
			LoginResponseDTO loginResponse = loginService.authenticateUser(loginRequestDTO);
			return ResponseEntity.ok(loginResponse);
		} catch (Exception e) {
			log.error("로그인 실패: {}", e.getMessage());
			// TODO: 실제 서비스에서는 정확한 오류 메시지를 주지 않는 것이 보안에 더 좋음
			return new ResponseEntity<>(LoginResponseDTO.builder().message(e.getMessage()).build(),
					HttpStatus.UNAUTHORIZED);
		}
	}

	// 테스트용 엔드포인트 (인증된 사용자만 접근 가능하도록 나중에 테스트해볼 것)
	@GetMapping("/me")
	public ResponseEntity<String> getMyInfo() {
		// 실제로는 SecurityContextHolder.getContext().getAuthentication().getName() 등으로 사용자
		// 정보를 가져올 수 있습니다.
		return ResponseEntity.ok("인증된 사용자만 접근할 수 있는 정보입니다.");
	}

}
