package com.bid.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEditRequestDTO {

	private String email;           // 수정할 이메일 (선택 사항)
    private String currentPassword; // 현재 비밀번호 (비밀번호 변경 시 필수)
    private String newPassword;     // 새 비밀번호 (비밀번호 변경 시 필수)
    
}
