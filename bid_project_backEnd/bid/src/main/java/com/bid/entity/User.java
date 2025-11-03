package com.bid.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "password", "userRoles", "userInterestBids" })
public class User {

	@Id // 기본 키
	@GeneratedValue(strategy = GenerationType.IDENTITY) // MariaDB의 AUTO_INCREMENT와 매핑
	@Column(name = "user_id") // 컬럼명
	private Long userId;

	@Column(name = "username", nullable = false, unique = true, length = 50)
	private String username;

	@Column(name = "password", nullable = false, length = 255)
	private String password; // 암호화된 비밀번호

	@Column(name = "email", unique = true, length = 100)
	private String email;

	@Column(name = "created_at", updatable = false) // 생성 시간은 업데이트되지 않음
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// ----- Auditing (생성/수정 시간 자동화) -----
	@PrePersist // 엔티티 저장 전 실행
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now(); // 초기 생성 시에도 updated_at 설정
	}

	@PreUpdate // 엔티티 업데이트 전 실행
	protected void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
	// ---------------------------------------------

	// --- Role 관련 (선택 사항: user_roles 테이블 사용 시) ---
	@ManyToMany(fetch = FetchType.EAGER) // Role은 User 조회 시 함께 로드 (인증 시 필요)
	@JoinTable(name = "user_roles", // 중간 테이블 이름
			joinColumns = @JoinColumn(name = "user_id"), // User 엔티티의 FK
			inverseJoinColumns = @JoinColumn(name = "role_id") // Role 엔티티의 FK
	)
	private Set<Role> userRoles = new HashSet<>();
	// ----------------------------------------------------

	// --- UserInterestBid 관련 ---
	// User와 UserInterestBid는 1:N 관계
	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	// "mappedBy"는 UserInterestBid 엔티티의 user 필드에 의해 매핑된다는 것을 의미
	// cascade=ALL: User 변경/삭제 시 InterestBid도 함께 처리
	// orphanRemoval=true: 연관관계가 끊어지면 고아 객체 자동 삭제
	private Set<UserInterestBid> userInterestBids = new HashSet<>();
}
