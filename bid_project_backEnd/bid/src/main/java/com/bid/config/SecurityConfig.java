package com.bid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.bid.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, // Spring 컨텍스트에서
																								// UserDetailsService 빈을
																								// 주입받음
			PasswordEncoder passwordEncoder) { // Spring 컨텍스트에서 PasswordEncoder 빈을 주입받음

		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userDetailsService); // 주입받은 userDetailsService 사용
		authenticationProvider.setPasswordEncoder(passwordEncoder); // 주입받은 passwordEncoder 사용

		return new ProviderManager(authenticationProvider);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()) // CSRF 보호 기능을 비활성화합니다.
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
				).authorizeHttpRequests(authorize -> authorize.requestMatchers("/api/**", "/login", "/signup")
						.permitAll().anyRequest().authenticated());

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // 예시

		return http.build();
	}

}
