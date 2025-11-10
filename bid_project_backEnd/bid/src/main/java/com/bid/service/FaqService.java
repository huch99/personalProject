package com.bid.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bid.dto.request.FaqRequestDTO;
import com.bid.dto.response.FaqResponseDTO;
import com.bid.entity.Faq;
import com.bid.entity.User;
import com.bid.repository.FaqRepository;
import com.bid.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

	private final FaqRepository faqRepository;
    private final UserRepository userRepository;
    
    public List<FaqResponseDTO> getAllFaqs() {
        return faqRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public FaqResponseDTO getFaqById(Long faqId) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> new EntityNotFoundException("FAQ를 찾을 수 없습니다. ID: " + faqId));
        return FaqResponseDTO.from(faq);
    }
    
    @Transactional // 쓰기 작업이므로 트랜잭션 필요
    public FaqResponseDTO createFaq(FaqRequestDTO requestDTO, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("작성자(User)를 찾을 수 없습니다. ID: " + userId));

        Faq newFaq = Faq.builder()
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .author(author)
                .build();
        Faq savedFaq = faqRepository.save(newFaq);
        return mapToDto(savedFaq);
    }
    
    @Transactional
    public FaqResponseDTO updateFaq(Long id, FaqRequestDTO requestDTO, Long userId) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FAQ를 찾을 수 없습니다. ID: " + id));

        // 작성자만 수정 가능하도록 검증
        if (!faq.getAuthor().getUserId().equals(userId)) {
            throw new IllegalArgumentException("FAQ를 수정할 권한이 없습니다.");
        }

        faq.setTitle(requestDTO.getTitle());
        faq.setContent(requestDTO.getContent());
        // faq.setUpdatedAt(LocalDateTime.now()); // @LastModifiedDate가 자동으로 처리

        Faq updatedFaq = faqRepository.save(faq);
        return mapToDto(updatedFaq);
    }
    
    @Transactional
    public void deleteFaq(Long id, Long userId) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("FAQ를 찾을 수 없습니다. ID: " + id));

        // 작성자만 삭제 가능하도록 검증
        if (!faq.getAuthor().getUserId().equals(userId)) {
            throw new IllegalArgumentException("FAQ를 삭제할 권한이 없습니다.");
        }

        faqRepository.delete(faq);
    }
    
 // Faq 엔티티를 FaqResponseDTO로 매핑하는 헬퍼 메서드
    private FaqResponseDTO mapToDto(Faq faq) {
        return FaqResponseDTO.builder()
                .faqId(faq.getFaqId())
                .title(faq.getTitle())
                .content(faq.getContent())
                .authorUsername(faq.getAuthor().getUsername()) // 작성자 User의 username 사용
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }
}
