package com.bid.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TenderRequestDTO {
	
	@NotBlank(message = "제목은 필수입니다.")
    private String tenderTitle;
	
	@NotBlank(message = "발주 기관은 필수입니다.")
    private String organization;
	
	@NotNull(message = "공고일은 필수입니다.")
    private LocalDateTime announcementDate;
	
	@NotNull(message = "마감일은 필수입니다.")
    private LocalDateTime deadline;
	
	private String description;
    private Long estimatedPrice;
    
    @NotBlank(message = "입찰 공고 번호는 필수입니다.")
    private String bidNumber;
    
}
