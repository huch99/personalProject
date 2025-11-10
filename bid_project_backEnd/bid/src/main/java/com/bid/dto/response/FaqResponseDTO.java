package com.bid.dto.response;

import java.time.LocalDateTime;

import com.bid.entity.Faq;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FaqResponseDTO {

	private Long faqId;
	private String title;
	private String content;
	private Long authorId;
	private String authorUsername; // 작성자 ID 대신 사용자 이름
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	public static FaqResponseDTO from(Faq faq) {
        return FaqResponseDTO.builder()
                .faqId(faq.getFaqId())
                .title(faq.getTitle())
                .content(faq.getContent())
                .authorUsername(faq.getAuthor().getUsername()) // User 엔티티에서 작성자 이름 가져오기
                .authorId(faq.getAuthor().getUserId()) // ✅ User 엔티티에서 작성자 ID 가져오기
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
