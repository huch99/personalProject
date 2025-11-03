package com.bid.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_interest_bids")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "user")
public class UserInterestBid {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_id")
    private Long interestId;

    @ManyToOne(fetch = FetchType.LAZY) // N:1 관계 (여러 관심 입찰이 한 User에 속함)
    @JoinColumn(name = "user_id", nullable = false) // user_id 컬럼과 매핑, FK
    private User user; // 연관된 User 엔티티

    @Column(name = "onbid_plnm_no", nullable = false)
    private Long onbidPlnmNo; // 온비드 API의 공고번호

    @Column(name = "cltr_nm", nullable = false, length = 1000)
    private String cltrNm; // 물건명 (캐싱)

    @Column(name = "ldnm_adrs", length = 1000)
    private String ldnmAdrs; // 물건소재지(지번) (캐싱)

    @Column(name = "min_bid_prc")
    private Long minBidPrc; // 최저입찰가 (캐싱)

    @Column(name = "pbct_begn_dtm")
    private LocalDateTime pbctBegnDt; // 입찰시작일시 (캐싱)

    @Column(name = "pbct_cls_dtm")
    private LocalDateTime pbctClsDt; // 입찰마감일시 (캐싱)

    @Column(name = "memo", columnDefinition = "TEXT") // TEXT 타입 매핑
    private String memo;

    @Column(name = "status", length = 50)
    private String status; // 관심 입찰 상태 (예: WISH, IN_PROGRESS, BID_COMPLETE)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ----- Auditing (생성/수정 시간 자동화) -----
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) { // 상태 기본값 설정
            this.status = "WISH";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    // ---------------------------------------------
    
}
