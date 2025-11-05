package com.bid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bid.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	// 사용자명(username)으로 사용자를 조회하는 커스텀 메서드
	Optional<User> findByUsername(String username);

	// 이메일로 User 엔티티를 찾는 메서드
	Optional<User> findByEmail(String email);

	// 사용자명 중복 체크를 위한 메서드
	boolean existsByUsername(String username);

	// 이메일 중복 체크를 위한 메서드 (이메일 사용 시)
	boolean existsByEmail(String email);
}
