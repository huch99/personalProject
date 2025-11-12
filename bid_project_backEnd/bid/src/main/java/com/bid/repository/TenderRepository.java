package com.bid.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bid.entity.Tender;

@Repository
public interface TenderRepository extends JpaRepository<Tender, Long> {
	
	List<Tender> findByDeadlineAfter(LocalDateTime date); // 특정 날짜 이후 마감인 입찰 조회

	List<Tender> findByOrganizationContaining(String keyword); // 기관명에 특정 키워드가 포함된 입찰 조회

	// cltrMnmtNo로 Tender를 찾는 메서드
    Optional<Tender> findByCltrMnmtNo(String cltrMnmtNo);
    
    // active 상태인 Tender들을 페이지네이션하여 조회
    Page<Tender> findByActiveTrue(Pageable pageable);
    
    Optional<Tender> findByTenderId(Long tenderId);
    
 // ✅ 상세 검색을 위한 쿼리 메서드 (다양한 조합이 가능하도록)
    // @Query 어노테이션을 사용하여 동적 쿼리를 작성합니다.
    @Query("SELECT t FROM Tender t WHERE t.active = true " +
           "AND (:cltrNm IS NULL OR LOWER(t.tenderTitle) LIKE LOWER(CONCAT('%', :cltrNm, '%'))) " + // 물건명 (부분 일치, 대소문자 무시)
           "AND (:dpslMtdNm IS NULL OR t.organization = :dpslMtdNm) " + // 처분방식명 (정확히 일치)
           // 시도, 시군구, 읍면동은 일단 tenderTitle(물건명)에서 함께 검색하는 방식으로 단순화 (주소 필드가 없으므로)
           // 나중에 Address 필드가 생기면 더욱 정교한 쿼리 가능
           "AND (:sido IS NULL OR LOWER(t.tenderTitle) LIKE LOWER(CONCAT('%', :sido, '%'))) " + // 시도
           "AND (:sgk IS NULL OR LOWER(t.tenderTitle) LIKE LOWER(CONCAT('%', :sgk, '%'))) " + // 시군구
           "AND (:emd IS NULL OR LOWER(t.tenderTitle) LIKE LOWER(CONCAT('%', :emd, '%'))) " + // 읍면동
           "AND (:minGoodsPrice IS NULL OR t.goodsName LIKE CONCAT('%총면적%', :minGoodsPrice, '%')) " + // 감정가 (goodsName에서 파싱 필요)
           "AND (:maxGoodsPrice IS NULL OR t.goodsName LIKE CONCAT('%총면적%', :maxGoodsPrice, '%')) " + // 감정가
           "AND (:pbctBegnDtm IS NULL OR t.announcementDate >= :pbctBegnDtm) " + // 공고 시작일 범위
           "AND (:pbctClsDtm IS NULL OR t.deadline <= :pbctClsDtm) ") // 공고 마감일 범위
    Page<Tender> searchTendersByCriteria(
            @Param("cltrNm") String cltrNm,
            @Param("dpslMtdNm") String dpslMtdNm, // DTO에서는 dpslMtdCd였지만 엔티티에서는 organization에 저장됩니다.
            @Param("sido") String sido,
            @Param("sgk") String sgk,
            @Param("emd") String emd,
            @Param("minGoodsPrice") Long minGoodsPrice, // Long으로 변경
            @Param("maxGoodsPrice") Long maxGoodsPrice, // Long으로 변경
            @Param("pbctBegnDtm") LocalDateTime pbctBegnDtm,
            @Param("pbctClsDtm") LocalDateTime pbctClsDtm,
            Pageable pageable);
}
