package com.bid.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bid.entity.Bid;
import com.bid.entity.User;

public interface BidRepository extends JpaRepository<Bid, Long> {

	List<Bid> findByUser(User user);
}
