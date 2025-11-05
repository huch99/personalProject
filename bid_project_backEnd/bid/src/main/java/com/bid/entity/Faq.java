package com.bid.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "faqs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 protected 기본 생성자 선호
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Faq {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long faqId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT") // TEXT 타입으로 설정
    private String content;

    // User와의 ManyToOne 관계 설정
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩
    @JoinColumn(name = "user_id", nullable = false) // FK는 user_id
    private User author; // FAQ 작성자

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Builder가 아닌 생성자로 DTO에서 엔티티 변환 시
    public Faq(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }
    
}
