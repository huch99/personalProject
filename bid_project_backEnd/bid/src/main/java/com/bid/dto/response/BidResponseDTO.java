package com.bid.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidResponseDTO {

	private Long bidId;
	private Long tenderId;
	private Long userId;
	private Long bidPrice;
	private LocalDateTime bidTime;
	private String message;
	
	private Long updatedMinBidPrice;
}
