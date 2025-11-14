package com.bid.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bid.dto.response.TenderResponseDTO;
import com.bid.entity.Favorite;
import com.bid.entity.Tender;
import com.bid.entity.User;
import com.bid.exception.ResourceNotFoundException;
import com.bid.repository.FavoriteRepository;
import com.bid.repository.TenderRepository;
import com.bid.repository.UserRepository;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FavoriteService {

	private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final TenderRepository tenderRepository;
    
 // 즐겨찾기 추가
    @Transactional
    public boolean addFavorite(Long userId, String cltrMnmtNo) {
    	log.info("FavoriteService - addFavorite entered. userId={}, cltrMnmtNo={}", userId, cltrMnmtNo);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        log.info("FavoriteService - User found: {}", user.getUsername());
        
        Tender tender = tenderRepository.findByCltrMnmtNo(cltrMnmtNo)
        		.orElseThrow(() -> {
                    log.error("FavoriteService - Tender not found for cltrMnmtNo: {}", cltrMnmtNo); // ✅ 로그
                    return new ResourceNotFoundException("Tender not found with cltrMnmtNo: " + cltrMnmtNo);
                });
        log.info("FavoriteService - Tender found: {}", tender.getTenderTitle());
        if (favoriteRepository.findByUserAndTender(user, tender).isPresent()) {
        	log.warn("FavoriteService - Favorite already exists for user {} and tender {}", userId, cltrMnmtNo);
        	return false; // 이미 즐겨찾기 됨
        }

        Favorite favorite = Favorite.builder()
                .user(user)
                .tender(tender)
                .build();
        favoriteRepository.save(favorite);
        log.info("FavoriteService - Favorite successfully added for user {} and tender {}", userId, cltrMnmtNo);
        return true;
    }

    // 즐겨찾기 삭제
    @Transactional
    public boolean removeFavorite(Long userId, String cltrMnmtNo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Tender tender = tenderRepository.findByCltrMnmtNo(cltrMnmtNo)
                .orElseThrow(() -> new ResourceNotFoundException("Tender not found with id: " + cltrMnmtNo));

        Optional<Favorite> favoriteOpt = favoriteRepository.findByUserAndTender(user, tender);
        if (favoriteOpt.isPresent()) {
            favoriteRepository.delete(favoriteOpt.get());
            return true;
        }
        return false; // 즐겨찾기 되어 있지 않음
    }

    // 특정 사용자의 특정 입찰 공고 즐겨찾기 여부 확인
    public boolean isFavorite(Long userId, String cltrMnmtNo) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Tender tender = tenderRepository.findByCltrMnmtNo(cltrMnmtNo)
                .orElseThrow(() -> new ResourceNotFoundException("Tender not found with id: " + cltrMnmtNo));
        return favoriteRepository.findByUserAndTender(user, tender).isPresent();
    }

    // 특정 사용자의 모든 즐겨찾기 목록 조회
    public List<TenderResponseDTO> getFavoriteTendersByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return favoriteRepository.findByUser(user).stream()
                .map(Favorite::getTender)
                .map(TenderResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
