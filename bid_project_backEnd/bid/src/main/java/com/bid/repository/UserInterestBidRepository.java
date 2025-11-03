package com.bid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bid.entity.UserInterestBid;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInterestBidRepository extends JpaRepository<UserInterestBid, Long> {

	 // 특정 사용자의 모든 관심 입찰을 조회
    List<UserInterestBid> findByUser_UserId(Long userId);

    // 특정 사용자의 특정 온비드 공고번호에 해당하는 관심 입찰 조회
    Optional<UserInterestBid> findByUser_UserIdAndOnbidPlnmNo(Long userId, Long onbidPlnmNo);

    // 특정 사용자가 특정 공고번호의 입찰을 이미 관심 등록했는지 확인
    boolean existsByUser_UserIdAndOnbidPlnmNo(Long userId, Long onbidPlnmNo);
}
