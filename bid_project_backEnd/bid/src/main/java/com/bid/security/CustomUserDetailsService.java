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
	 
	/**
     * username (사용자명)을 기반으로 UserDetails 객체를 로드합니다.
     * Spring Security의 AuthenticationManager가 사용자 인증 시 호출합니다.
     * @param username the username identifying the user whose data is required.
     * @return a fully populated user record (never null)
     * @throws UsernameNotFoundException if the user could not be found or has no granted authorities
     */
    @Override
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 설정
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 데이터베이스에서 username에 해당하는 User 엔티티 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // User 엔티티의 권한 정보를 GrantedAuthority 컬렉션으로 변환
        // TODO: (선택 사항) User 엔티티에 직접 Role 정보를 추가하는 경우 getAuthoritiesFromUserRole(user) 구현
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername()) // 사용자명
                .password(user.getPassword()) // 암호화된 비밀번호
                // TODO: 현재 Role 엔티티가 없다고 가정하고, 기본 'ROLE_USER' 권한을 부여합니다.
                // Spring Security 설정에 따라 실제 유저의 권한을 가져오도록 수정해야 합니다.
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))) // 권한 설정 예시
                .build();
    }
    
    // (선택 사항) User 엔티티에 Role 정보가 직접 매핑되어 있을 경우 (예: User 엔티티에 Set<Role> roles 필드가 있다면)
    /*
    private Collection<? extends GrantedAuthority> getAuthoritiesFromUserRoles(User user) {
        return user.getUserRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName()))
                .collect(Collectors.toList());
    }
    */
    
}
