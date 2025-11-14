package com.bid.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BidRequestDTO {

	@NotNull(message = "입찰할 공고 ID는 필수입니다.")
	private Long tenderId;
	
	@Min(value = 1, message = "입찰 가격은 1원 이상이어야 합니다.")
	private Long bidPrice;
	
	@Builder
	public BidRequestDTO(Long tenderId, Long bidPrice) {
		this.tenderId = tenderId;
		this.bidPrice = bidPrice;
	}
}
