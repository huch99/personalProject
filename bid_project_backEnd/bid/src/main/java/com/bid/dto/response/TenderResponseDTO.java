package com.bid.dto.response;

import java.time.LocalDateTime;

import com.bid.entity.Tender;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenderResponseDTO {

	private Long tenderId; // 입찰 고유 ID
	private Long pbctNo; // ✅ 추가! PBCT_NO 매핑
	private String cltrHstrNo; // ✅ 추가! CLTR_HSTR_NO 매핑
	private String cltrMnmtNo;
	private String tenderTitle; // 입찰 공고 제목
	private String organization; // 발주 기관
	private String bidNumber; // 입찰 공고 번호
	private String goodsName;
	private Long minBidPrice; // MIN_BID_PRC (최저 입찰가)
	private Long apslAsesAvgAmt;
	private Long openPriceFrom;
	private Long openPriceTo;
	
	private LocalDateTime lastSyncedAt;
    private Boolean active;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // LocalDateTime 직렬화 형식 지정
	private LocalDateTime announcementDate; // 공고일

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss") // LocalDateTime 직렬화 형식 지정
	private LocalDateTime deadline; // 입찰 마감일
	
	private String status;

	// 엔티티를 DTO로 변환하는 정적 팩토리 메서드 (간단한 매퍼 역할)
	public static TenderResponseDTO fromEntity(Tender tender) {
		
		LocalDateTime now = LocalDateTime.now();
		
		String status;
		
		if(tender.getAnnouncementDate() == null || tender.getDeadline() == null) {
			status = "알 수 없음";
		} else if (now.isBefore(tender.getAnnouncementDate())) {
			status = "입찰 예정";
		} else if (now.isAfter(tender.getDeadline())) {
			status = "입찰 마감";
		} else {
			status = "입찰 진행 중";
		}
		return TenderResponseDTO.builder().tenderId(tender.getTenderId()).pbctNo(tender.getPbctNo())
				.cltrHstrNo(tender.getCltrHstrNo()).cltrMnmtNo(tender.getCltrMnmtNo())
				.tenderTitle(tender.getTenderTitle()).organization(tender.getOrganization())
				.bidNumber(tender.getBidNumber()).announcementDate(tender.getAnnouncementDate())
				.goodsName(tender.getGoodsName()).deadline(tender.getDeadline())
				.openPriceFrom(tender.getInitialOpenPriceFrom())
				.openPriceTo(tender.getInitialOpenPriceTo())
				.lastSyncedAt(tender.getLastSyncedAt())
                .active(tender.isActive())
                .status(status)
				.build();
	}

}
