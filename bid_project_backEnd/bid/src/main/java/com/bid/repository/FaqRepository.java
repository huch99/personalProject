package com.bid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bid.entity.Faq;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

}
