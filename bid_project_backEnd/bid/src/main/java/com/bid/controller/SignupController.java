package com.bid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bid.dto.request.SignupRequestDTO;
import com.bid.dto.response.SignupResponseDTO;
import com.bid.exception.SignupException;
import com.bid.service.SignupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/signup") // 회원가입 API 엔드포인트
@RequiredArgsConstructor // SignupService를 주입받기 위한 Lombok 어노테이션
@Slf4j // 로깅을 위한 Lombok 어노테이션
@CrossOrigin(origins = "http://localhost:5173") // Huch님의 프론트엔드 CORS 허용
public class SignupController {

	private final SignupService signupService;
	
	 @PostMapping
	    public ResponseEntity<SignupResponseDTO> signupUser(@Valid @RequestBody SignupRequestDTO signupRequestDTO,
	                                                         BindingResult bindingResult) {
	        // @Valid 어노테이션으로 SignupRequestDTO에 대한 유효성 검사 수행
	        // BindingResult를 통해 유효성 검사 결과를 받음

	        // 1. 유효성 검사 실패 시 처리
	        if (bindingResult.hasErrors()) {
	            // 필드 에러 메시지를 조합하여 반환
	            String errorMessage = bindingResult.getFieldErrors().stream()
	                    .map(fieldError -> fieldError.getDefaultMessage())
	                    .collect(java.util.stream.Collectors.joining(", "));
	            log.warn("회원가입 유효성 검사 실패: {}", errorMessage);
	            return ResponseEntity.badRequest().body(new SignupResponseDTO(false, errorMessage, null, null));
	        }

	        try {
	            // 2. SignupService를 통해 회원가입 처리
	            SignupResponseDTO response = signupService.registerUser(signupRequestDTO);
	            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created 반환
	        } catch (SignupException e) {
	            // 3. 비즈니스 로직 예외 처리 (예: 아이디/이메일 중복)
	            log.warn("회원가입 비즈니스 로직 예외: {}", e.getMessage());
	            return ResponseEntity.status(HttpStatus.CONFLICT).body(new SignupResponseDTO(false, e.getMessage(), null, null)); // 409 Conflict 반환
	        } catch (Exception e) {
	            // 4. 그 외 예상치 못한 예외 처리
	            log.error("회원가입 처리 중 알 수 없는 오류 발생: {}", e.getMessage(), e);
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new SignupResponseDTO(false, "회원가입 중 서버 오류가 발생했습니다.", null, null)); // 500 Internal Server Error 반환
	        }
	    }
}
