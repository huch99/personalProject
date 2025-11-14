package com.bid.service;

import java.time.LocalDateTime;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bid.dto.request.BidRequestDTO;
import com.bid.dto.response.BidResponseDTO;
import com.bid.entity.Bid;
import com.bid.entity.Tender;
import com.bid.entity.User;
import com.bid.repository.BidRepository;
import com.bid.repository.TenderRepository;
import com.bid.repository.UserRepository;
import com.bid.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BidService {

	private final TenderRepository tenderRepository;
	private final UserRepository userRepository;
	private final BidRepository bidRepository;
	
	@Transactional
	public BidResponseDTO placeBid(BidRequestDTO bidRequestDTO) {
		Long currentUserId = getCurrentUserId();
		
		Tender tender = tenderRepository.findById(bidRequestDTO.getTenderId())
				.orElseThrow(() -> new IllegalArgumentException("존재하지 않는 입찰 공고입니다."));
		
		User user = userRepository.findById(currentUserId)
				.orElseThrow(() -> new IllegalArgumentException("인증된 사용자 정보를 찾을 수 없습니다."));
	
		Long currentMinBidPrice = tender.getMinBidPrice();
		Long userBidPrice = bidRequestDTO.getBidPrice();
		
		if(currentMinBidPrice == null || currentMinBidPrice == 0L) {
			if(userBidPrice <= 0) {
				throw new IllegalArgumentException("입찰 가격은 0보다 커야 합니다.");
			}
			if(tender.getInitialOpenPriceFrom() != null && userBidPrice < tender.getInitialOpenPriceFrom()) {
				throw new IllegalArgumentException("입찰 가격은 공고의 최초 최저 입찰가(" + tender.getInitialOpenPriceFrom() + ")보다 낮을 수 없습니다.");
			}
		} else {
			if (userBidPrice <= currentMinBidPrice) {
				throw new IllegalArgumentException("입찰 가격은 현재 최저 입찰가(" + currentMinBidPrice + ")보다 높아야 합니다.");
			}
		}
		
		tender.setMinBidPrice(userBidPrice);
		
		Bid bid = Bid.builder()
				.tender(tender)
				.user(user)
				.bidPrice(userBidPrice)
				.bidTime(LocalDateTime.now())
				.build();
		Bid savedBid = bidRepository.save(bid);
		
		LocalDateTime now = LocalDateTime.now();
		
		if(tender.getAnnouncementDate() == null || tender.getDeadline() ==  null || now.isBefore(tender.getAnnouncementDate()) || now.isAfter(tender.getDeadline())) {
			String currentStatus;
			if(tender.getAnnouncementDate() == null || tender.getDeadline() == null) currentStatus = "알 수 없음";
			else if (now.isBefore(tender.getAnnouncementDate())) currentStatus = "입찰 예정";
			else if (now.isAfter(tender.getDeadline())) currentStatus = "입찰 마감";
			else currentStatus = "입찰 진행중";
			
			if (!"입찰 진행중".equals(currentStatus)) {
				throw new IllegalArgumentException("현재 " + currentStatus + "인 공고에는 입찰할 수 없습니다.");
			}
		}
		
		return BidResponseDTO.builder()
				.bidId(savedBid.getBidId())
				.tenderId(savedBid.getTender().getTenderId())
				.userId(savedBid.getUser().getUserId())
				.bidPrice(savedBid.getBidPrice())
				.bidTime(savedBid.getBidTime())
				.message("입찰이 성공적으로 등록 되었습니다.")
				.updatedMinBidPrice(tender.getMinBidPrice())
				.build();
	}
	
	private Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
			throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다. JWT 토큰을 확인 해주세요.");
		}
		
		return ((CustomUserDetails) authentication.getPrincipal()).getUserId()
;	}
}
