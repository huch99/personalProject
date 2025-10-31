package com.bid.dto.response;

import java.time.LocalDateTime;

import com.bid.entity.Tender;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TenderResponseDTO {
	
	private Long tenderId; // 입찰 고유 ID
	private String tenderTitle; // 입찰 공고 제목
	private String organization; // 발주 기관
	private String bidNumber; // 입찰 공고 번호
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // LocalDateTime 직렬화 형식 지정
    private LocalDateTime announcementDate; // 공고일
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // LocalDateTime 직렬화 형식 지정
    private LocalDateTime deadline; // 입찰 마감일
	
	// 엔티티를 DTO로 변환하는 정적 팩토리 메서드 (간단한 매퍼 역할)
    public static TenderResponseDTO from(Tender tender) {
        return TenderResponseDTO.builder()
                .tenderId(tender.getTenderId())
                .tenderTitle(tender.getTenderTitle())
                .organization(tender.getOrganization())
                .bidNumber(tender.getBidNumber())
                .announcementDate(tender.getAnnouncementDate())
                .deadline(tender.getDeadline())
                .build();
    }
    
}
