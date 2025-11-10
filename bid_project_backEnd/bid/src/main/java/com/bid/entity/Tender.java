package com.bid.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "tenders")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Lombok: 인자 없는 생성자 (JPA 필수)
@AllArgsConstructor
@Builder
public class Tender {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long tenderId; // 입찰 고유 아이디
	
	@Column(nullable = false, length = 500)
	private String tenderTitle;
	
	@Column
	private Long pbcdNo;
	
	@Column
	private String cltrMnmtNo;
	
	@Column
	private String cltrHstrNo;
	
	@Column(nullable = false)
    private String organization; // 발주 기관 (예: 한국자산관리공사)
	
	@Column(nullable = false)
    private LocalDateTime announcementDate; // 공고일

    @Column(nullable = false)
    private LocalDateTime deadline; // 입찰 마감일
    
    @Column
    private String goodsName;
    
    @Column(nullable = true, length = 1000)
    private String description; // 상세 내용 (선택 사항)

    @Column(nullable = true)
    private Long estimatedPrice; // 예정 가격 (선택 사항)

    @Column(nullable = false)
    private String bidNumber; // 입찰 공고 번호 (고유값으로 관리)
}
