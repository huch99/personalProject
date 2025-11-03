package com.bid.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OnbidHeader {

	@JacksonXmlProperty(localName = "resultCode")
	private String resultCode;
	
	@JacksonXmlProperty(localName = "resultMsg")
	private String resultMsg;
}
