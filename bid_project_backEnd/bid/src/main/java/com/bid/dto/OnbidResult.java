package com.bid.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OnbidResult {

	private OnbidHeader header;
	private OnbidBody body;
}
