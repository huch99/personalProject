package com.bid.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bid.dto.request.UserEditRequestDTO;
import com.bid.dto.response.UserProfileResponseDTO;
import com.bid.entity.User;
import com.bid.repository.UserRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserProfileResponseDTO getUserProfile(String username) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
		return UserProfileResponseDTO.fromEntity(user);
	}

	@Transactional
	public UserProfileResponseDTO updateUserInfo(String username, UserEditRequestDTO request) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

		// 이메일 변경
		if (request.getEmail() != null && !request.getEmail().isEmpty()) {
			user.setEmail(request.getEmail());
		}

		User updatedUser = userRepository.save(user); // 변경된 내용 저장
		return UserProfileResponseDTO.fromEntity(updatedUser);
	}

	// 비밀번호 변경
	@Transactional
	public void changePassword(String username, String currentPassword, String newPassword) {
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

		// 현재 비밀번호 확인
		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
		}

		// 새 비밀번호 암호화 및 저장
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}
}
