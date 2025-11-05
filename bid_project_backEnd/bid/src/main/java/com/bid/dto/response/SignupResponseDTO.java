package com.bid.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponseDTO {

	private boolean success; // 회원가입 성공 여부
    private String message;  // 사용자에게 보여줄 메시지 (예: "회원가입이 성공적으로 완료되었습니다.")
    private Long userId;     // 가입된 사용자 ID
    private String username; // 가입된 사용자 이름
    
}
