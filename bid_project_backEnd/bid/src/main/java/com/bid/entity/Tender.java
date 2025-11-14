package com.bid.entity;

import java.time.LocalDateTime;

import com.bid.dto.response.TenderResponseDTO;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "tenders", indexes = { // 인덱스 추가 (조회 성능 향상 및 unique 제약 조건)
	    @Index(name = "idx_cltr_mnmt_no", columnList = "cltrMnmtNo", unique = true),
	    @Index(name = "idx_announcement_date", columnList = "announcementDate") // 정렬을 위해 인덱스 추가
	})
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok: 인자 없는 생성자 (JPA 필수)
@AllArgsConstructor
@Builder
public class Tender {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본키 자동 생성 전략
    private Long id; // 우리 DB에서 사용하는 고유 ID (내부용)

    @Column(nullable = false, unique = true, length = 50) 
    private String cltrMnmtNo; // 물건관리번호 (CLTR_MNMT_NO) - 온비드 내에서 고유하다고 가정

    private Long tenderId;       // 입찰 고유 ID (PLNM_NO)
    private Long pbctNo;         // 공매번호 (PBCT_NO)
    private String cltrHstrNo; 	// CLTR_HSTR_NO
    private String tenderTitle;  // 입찰 공고 제목 (CLTR_NM)
    private String organization; // 처분방식명 (DPSL_MTD_NM)
    private String bidNumber;    // 입찰 공고 번호 (BID_MNMT_NO)
    @Column(columnDefinition = "TEXT") // 설명이 길 수 있으므로 TEXT 타입 고려
    private String goodsName;    // 물건 상세 설명 (GOODS_NM)
    private Long minBidPrice; // MIN_BID_PRC (최저 입찰가)
    private Long apslAsesAvgAmt; // APSL_ASES_AVG_AMT (감정평가액)
    
    private LocalDateTime announcementDate; // 공고일 (PBCT_BEGN_DTM)
    private LocalDateTime deadline;       // 입찰 마감일 (PBCT_CLS_DTM)
    
    private Long initialOpenPriceFrom;
    private Long initialOpenPriceTo;

    // 추가 메타데이터 (데이터 동기화 관리를 위해)
    private LocalDateTime lastSyncedAt; // 이 레코드가 온비드와 마지막으로 동기화된 시간
    private boolean active; // 현재 활성 상태인지 여부 (예: 기간 만료/삭제된 공고 처리)

    // TenderResponseDTO에서 Tender 엔티티로 변환하는 팩토리 메서드 (선택 사항, Mapper로 대체 가능)
    public static Tender fromDto(TenderResponseDTO dto) {
        return Tender.builder()
                .tenderId(dto.getTenderId())
                .pbctNo(dto.getPbctNo())
                .cltrHstrNo(dto.getCltrHstrNo())
                .cltrMnmtNo(dto.getCltrMnmtNo())
                .tenderTitle(dto.getTenderTitle())
                .organization(dto.getOrganization())
                .bidNumber(dto.getBidNumber())
                .goodsName(dto.getGoodsName())
                .announcementDate(dto.getAnnouncementDate())
                .deadline(dto.getDeadline())
                .minBidPrice(dto.getMinBidPrice())   
                .apslAsesAvgAmt(dto.getApslAsesAvgAmt())
                .lastSyncedAt(LocalDateTime.now()) // 생성 시점 기록
                .active(true) // 기본적으로 활성 상태로 생성
                .initialOpenPriceFrom(dto.getOpenPriceFrom())
                .initialOpenPriceTo(dto.getOpenPriceTo())
                .build();
    }
}
