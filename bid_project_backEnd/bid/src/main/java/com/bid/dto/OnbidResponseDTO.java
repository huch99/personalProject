package com.bid.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JacksonXmlRootElement(localName = "response")
public class OnbidResponseDTO {
	
	@JacksonXmlProperty(localName = "header")
	private OnbidHeader header;
	
	@JacksonXmlProperty(localName = "body")
	private OnbidBody body;
}
