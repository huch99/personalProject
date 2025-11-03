package com.bid.dto;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OnbidBody {

	@JacksonXmlProperty(localName = "pageNo")
	private Integer pageNo; // 페이지 번호

	@JacksonXmlProperty(localName = "totalCount") // XML에서는 totalCount로 나옴
	private Integer totalCount; // 총 건수 (totalCount 태그)

	@JacksonXmlProperty(localName = "numOfRows")
	private Integer numOfRows; // 한 페이지 결과 수

	@JacksonXmlElementWrapper(localName = "items") // 👈 <items> 태그로 List를 감쌈
	@JacksonXmlProperty(localName = "item") // 👈 각 리스트 요소가 <item> 태그
	private List<OnbidItem> items; // 실제 입찰 목록 아이템들
}
