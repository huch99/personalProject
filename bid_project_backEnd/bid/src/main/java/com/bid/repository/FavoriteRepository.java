package com.bid.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bid.entity.Favorite;
import com.bid.entity.Tender;
import com.bid.entity.User;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

	// 특정 사용자와 특정 입찰 공고에 대한 즐겨찾기가 있는지 확인 (존재 여부 또는 단일 객체 조회)
    Optional<Favorite> findByUserAndTender(User user, Tender tender);

    // 특정 사용자의 모든 즐겨찾기 목록 조회
    List<Favorite> findByUser(User user);

    // 특정 사용자가 즐겨찾기한 특정 입찰 공고를 삭제
    void deleteByUserAndTender(User user, Tender tender);
    
}
