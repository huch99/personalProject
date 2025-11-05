package com.bid.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bid.dto.request.SignupRequestDTO;
import com.bid.dto.response.SignupResponseDTO;
import com.bid.entity.Role;
import com.bid.entity.User;
import com.bid.exception.SignupException;
import com.bid.repository.RoleRepository;
import com.bid.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor // UserRepository, PasswordEncoder 등을 주입받기 위한 Lombok 어노테이션
@Slf4j // 로깅을 위한 Lombok 어노테이션
public class SignupService {

	private final UserRepository userRepository; // User 엔티티를 저장할 Repository
	private final RoleRepository roleRepository; // 사용자 역할을 찾을 Repository
	private final PasswordEncoder passwordEncoder; // 비밀번호 암호화 인코더

	@Transactional // 회원가입 트랜잭션 (사용자 저장 및 역할 부여 등)
    public SignupResponseDTO registerUser(SignupRequestDTO signupRequestDTO) {
        log.info("회원가입 시도: username={}, email={}", signupRequestDTO.getUsername(), signupRequestDTO.getEmail());

        // 1. 아이디 중복 확인
        if (userRepository.findByUsername(signupRequestDTO.getUsername()).isPresent()) {
            throw new SignupException("이미 존재하는 아이디입니다.");
        }

        // 2. 이메일 중복 확인
        if (userRepository.findByEmail(signupRequestDTO.getEmail()).isPresent()) {
            throw new SignupException("이미 사용 중인 이메일입니다.");
        }

        // 3. 비밀번호 암호화 (Huch님 User 엔티티에 맞춰 필드명 'password' 사용)
        String encodedPassword = passwordEncoder.encode(signupRequestDTO.getPassword());

        // 4. 기본 역할(Role) 할당 (예: USER_ROLE)
        // Spring Security를 사용하고 계실 경우 기본 역할이 필요합니다.
        Role userRole = roleRepository.findByRoleName("ROLE_USER") // 'ROLE_USER'라는 이름의 역할 찾기
                .orElseThrow(() -> new SignupException("기본 역할(ROLE_USER)을 찾을 수 없습니다."));
        Set<Role> roles = new HashSet<>(Collections.singletonList(userRole));


        // 5. User 엔티티 생성
        User newUser = User.builder()
                .username(signupRequestDTO.getUsername())
                .password(encodedPassword) // 암호화된 비밀번호 저장
                .email(signupRequestDTO.getEmail())
                // User 엔티티에 @PrePersist가 있으면 createdAt/updatedAt은 자동 설정됩니다.
                // .createdAt(LocalDateTime.now())
                // .updatedAt(LocalDateTime.now())
                .userRoles(roles) // 역할 설정
                .build();

        // 6. User 엔티티 저장
        User savedUser = userRepository.save(newUser);
        log.info("회원가입 성공: userId={}, username={}", savedUser.getUserId(), savedUser.getUsername());

        // 7. SignupResponseDTO 생성 및 반환
        return new SignupResponseDTO(true, "회원가입이 성공적으로 완료되었습니다.", savedUser.getUserId(), savedUser.getUsername());
    }

    // TODO: Huch님 User 엔티티에 맞춰 User.builder()가 작동하려면
    // User 엔티티 클래스에 @Builder 어노테이션이 필요합니다.
    // 만약 @Builder가 없다면 아래와 같이 직접 User 객체를 생성해야 합니다.
    /*
    private User buildUser(SignupRequestDTO signupRequestDTO, String encodedPassword, Set<Role> roles) {
        User user = new User();
        user.setUsername(signupRequestDTO.getUsername());
        user.setPassword(encodedPassword);
        user.setEmail(signupRequestDTO.getEmail());
        user.setUserRoles(roles);
        return user;
    }
    */

}
