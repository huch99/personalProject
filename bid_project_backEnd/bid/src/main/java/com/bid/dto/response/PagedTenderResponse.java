package com.bid.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedTenderResponse {

	private List<TenderResponseDTO> tenders; // 현재 페이지의 입찰 목록
    private int totalCount;                  // 전체 입찰 건수
    private int pageNo;                      // 현재 페이지 번호
    private int numOfRows;
    
}
