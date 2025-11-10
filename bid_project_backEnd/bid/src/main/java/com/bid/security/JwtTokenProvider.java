package com.bid.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.bid.config.JwtProperties;

import jakarta.annotation.PostConstruct; // Spring Boot 3.x에서는 jakarta.annotation.* 사용
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j // Lombok을 이용한 로거
@Component
public class JwtTokenProvider {

	private final JwtProperties jwtProperties;
    private Key key; // JWT 서명에 사용될 비밀 키 객체

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    // Bean 초기화 시 secretKey를 디코딩하여 Key 객체 생성
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName(); // UserDetails의 username
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); // 사용자의 권한들을 콤마로 구분하여 문자열로 저장

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .setSubject(username) // 토큰의 주체 (여기서는 사용자 이름)
                .claim("roles", authorities) // 커스텀 클레임으로 사용자 권한 추가
                .setIssuedAt(now) // 발행 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(key, SignatureAlgorithm.HS512) // 비밀 키와 알고리즘으로 서명
                .compact(); // JWT를 문자열로 직렬화
    }
    
    public String generateToken(UserDetails userDetails) {
        String username = userDetails.getUsername();
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key) // 서명 검증을 위한 비밀 키
                .build()
                .parseClaimsJws(token) // 토큰 파싱
                .getBody(); // 클레임 바디 얻기

        return claims.getSubject(); // subject 클레임 반환
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException ex) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException ex) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException ex) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException ex) {
            log.error("JWT 클레임 문자열이 비어있습니다.");
        }
        return false;
    }

    public Collection<? extends GrantedAuthority> getAuthorities(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return Arrays.stream(claims.get("roles").toString().split(",")) // roles 클레임 파싱
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
    
}
