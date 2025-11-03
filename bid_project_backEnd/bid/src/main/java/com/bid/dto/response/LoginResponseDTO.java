package com.bid.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponseDTO {

	private String accessToken;
    private String message;
    private String username;
    private String password;
    private Long userId;
    
}
