package com.bid.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "jwt") // application.properties에서 'jwt.'로 시작하는 속성 매핑
@Getter
@Setter
public class JwtProperties {

	private String secretKey; // JWT 서명에 사용할 비밀 키
	private long accessTokenExpirationMs; // Access Token 만료 시간 (밀리초)
	private long refreshTokenExpirationMs; // Refresh Token 만료 시간 (밀리초) - 선택 사항
}
