package com.bid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**") // 모든 경로에 대해 CORS 허용
				.allowedOrigins("http://localhost:5173") // React 개발 서버 주소
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드
				.allowedHeaders("*") // 모든 헤더 허용
				.allowCredentials(true) // 자격 증명 (쿠리, HTTP 인증 등) 허용
				.maxAge(3600); // Preflight 요청 결과 캐싱 시간 (초)
	}
	
}
