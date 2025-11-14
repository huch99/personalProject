package com.bid.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bids")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bid {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long bidId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tender_id", nullable = false)
	private Tender tender;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Column(nullable = false)
	private Long bidPrice;
	
	@Column(nullable = false)
	private LocalDateTime bidTime;
}
