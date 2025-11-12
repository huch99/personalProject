package com.bid.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.bid.dto.request.LoginRequestDTO;
import com.bid.dto.request.SignupRequestDTO;
import com.bid.dto.response.LoginResponseDTO;
import com.bid.entity.User;
import com.bid.security.JwtTokenProvider;
import com.bid.service.LoginService;
import com.bid.service.UserService;

@RestController // RESTful API 컨트롤러
@RequestMapping("/api/login") // 기본 URL 경로
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

	private final LoginService loginService;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserService userService;

	@PostMapping
	public ResponseEntity<LoginResponseDTO> authenticateUser(@RequestBody LoginRequestDTO loginRequestDTO) {
		log.info("로그인 요청: {}", loginRequestDTO.getUsername());
		try {
//			LoginResponseDTO loginResponse = loginService.authenticateUser(loginRequestDTO);
			
			Authentication authentication = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(
	                        loginRequestDTO.getUsername(),
	                        loginRequestDTO.getPassword()
	                )
	        );
			
			SecurityContextHolder.getContext().setAuthentication(authentication);

	        String jwt = jwtTokenProvider.generateToken(authentication);
	        
	        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
	        
	        User user = userService.getUserRepository().findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));
//			return ResponseEntity.ok(loginResponse);
	        return ResponseEntity.ok(LoginResponseDTO.builder()
	                .accessToken(jwt)
	                .username(user.getUsername())
	                .userId(user.getUserId()) // UserDetails에서 ID를 직접 제공하지 않는다면 User 엔티티에서 가져와야 함
	                .email(user.getEmail()) // ✅ User 엔티티에서 email 가져와 설정
	                .build());
		} catch (Exception e) {
			log.error("로그인 실패: {}", e.getMessage());
			// TODO: 실제 서비스에서는 정확한 오류 메시지를 주지 않는 것이 보안에 더 좋음
			return new ResponseEntity<>(LoginResponseDTO.builder().message(e.getMessage()).build(),
					HttpStatus.UNAUTHORIZED);
		}
	}

}
