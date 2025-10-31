package com.bid.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OnbidBody {

	private List<OnbidItem> items;
	private int totalCount;
	private int pageNo;
	private int numOfRows;
}
