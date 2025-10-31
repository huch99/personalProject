package com.bid.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bid.entity.Tender;

@Repository
public interface TenderRepository extends JpaRepository<Tender, Long> {
	
	List<Tender> findByDeadlineAfter(LocalDateTime date); // 특정 날짜 이후 마감인 입찰 조회

	List<Tender> findByOrganizationContaining(String keyword); // 기관명에 특정 키워드가 포함된 입찰 조회
}
