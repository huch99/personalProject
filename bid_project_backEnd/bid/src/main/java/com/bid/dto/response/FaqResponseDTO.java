package com.bid.dto.response;

import java.time.LocalDateTime;

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
	private String authorUsername; // 작성자 ID 대신 사용자 이름
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
