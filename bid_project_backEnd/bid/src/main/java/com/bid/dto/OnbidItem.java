package com.bid.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OnbidItem {

	private String bidNo; // 입찰 번호 (공공 API 고유 ID)
	private String bidNm; // 입찰명
	private String orgNm; // 발주기관명
	private String ntceDt; // 공고일시 (String 형태로 오는 경우가 많음)
	private String clsgDt; // 마감일시 (String 형태로 오는 경우가 많음)
	private String detailLink;
}
