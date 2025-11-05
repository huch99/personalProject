package com.bid.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 이 부분은 Huch님의 DB에서 실제 사용자 정보를 조회하는 로직으로 대체되어야 합니다.
        // 현재는 예시를 위해 임시로 "user"라는 사용자만 있다고 가정합니다.
        if ("user".equals(username)) {
            // "password"는 반드시 인코딩된 비밀번호여야 합니다.
            // 지금은 임시로 'passwordEncoder().encode("1234")'를 사용해 생성된 해시값을 넣어보세요.
            // 예를 들어, "secret"을 BCryptPasswordEncoder로 인코딩한 결과: $2a$10$T85Xz.z.X.X.X.X.X.X.X.X.X.X.X
            return User.builder()
                    .username("user")
                    .password("$2a$10$J52mNq4f.D.T.sT7.t.w.A.w.E.S.d.f.e.H.A") // 실제 BCrypt로 인코딩된 "1234" 비밀번호
                    .roles("USER") // 사용자 역할 설정
                    .build();
        } else {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
    }
}
