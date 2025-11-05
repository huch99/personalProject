import React, { useState } from 'react';
import styled from 'styled-components';

const ModalOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
`;

const ModalContent = styled.div`
  position: relative;
  background-color: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 400px;
  max-width: 90%;
  z-index: 1001;
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

const ModalTitle = styled.h2`
  margin-bottom: 20px;
  color: #333;
  text-align: center;
`;

const SignupForm = styled.form`
  display: flex;
  flex-direction: column;
  gap: 15px;
`;

const InputField = styled.div`
  display: flex;
  flex-direction: column;
`;

const Label = styled.label`
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
`;

const Input = styled.input`
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;

  &:focus {
    border-color: #28a745; /* 회원가입 관련 색상 (초록색 계열) */
    outline: none;
    box-shadow: 0 0 0 2px rgba(40, 167, 69, 0.25);
  }
`;

const SubmitButton = styled.button`
  padding: 12px 20px;
  background-color: #28a745; /* 초록색 계열 */
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s ease;

  &:hover {
    background-color: #218838;
  }
`;

const ErrorMessage = styled.p`
  color: red;
  text-align: center;
  margin-top: -10px;
  font-size: 14px;
`;

const SuccessMessage = styled.p`
  color: green;
  text-align: center;
  margin-top: -10px;
  font-size: 14px;
`;

const CloseButton = styled.button`
  position: absolute;
  top: 10px;
  right: 10px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #888;

  &:hover {
    color: #333;
  }
`;

const SignupModal = ({ isOpen, onClose, onSignupSuccess }) => {
    // 상태 변수
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [passwordConfirm, setPasswordConfirm] = useState(''); // 비밀번호 확인 필드 추가
    const [email, setEmail] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isSigningUp, setIsSigningUp] = useState(false); // 회원가입 진행 중 상태

    if (!isOpen) return null;

    const handleSubmit = async (e) => {
        e.preventDefault(); // 폼의 기본 제출 동작 방지

        // 1. 클라이언트 측 유효성 검사 (SignupRequestDTO의 서버 측 유효성 검사와 일치하도록)
        if (!username.trim() || !password.trim() || !passwordConfirm.trim() || !email.trim()) {
            setErrorMessage('모든 필드를 입력해주세요.');
            return;
        }
        if (username.length < 4 || username.length > 20) {
            setErrorMessage('아이디는 4자 이상 20자 이하로 입력해주세요.');
            return;
        }
        if (password.length < 8 || password.length > 30) {
            setErrorMessage('비밀번호는 8자 이상 30자 이하로 입력해주세요.');
            return;
        }
        if (password !== passwordConfirm) {
            setErrorMessage('비밀번호와 비밀번호 확인이 일치하지 않습니다.');
            return;
        }
        // 간단한 이메일 형식 검사 (더 강력한 정규식 필요시 추가)
        if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
            setErrorMessage('유효한 이메일 주소를 입력해주세요.');
            return;
        }

        setErrorMessage(''); // 이전 에러 메시지 초기화
        setSuccessMessage(''); // 이전 성공 메시지 초기화
        setIsSigningUp(true); // 회원가입 진행 중으로 설정

        // 2. 백엔드 회원가입 API 호출
        try {
            const response = await fetch('http://localhost:8080/api/signup', { // TODO: 여기에 Huch님의 실제 회원가입 API 엔드포인트 URL을 입력해주세요.
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ username, password, email }), // SignupRequestDTO에 맞춰 데이터 전송
            });

            // 3. 백엔드 응답 처리
            const data = await response.json(); // 백엔드 응답이 JSON 형식이라고 가정 (SignupResponseDTO)

            if (response.ok && data.success) { // 회원가입 성공
                console.log('회원가입 성공:', data.message);
                setSuccessMessage(data.message || '회원가입이 성공적으로 완료되었습니다.');
                onSignupSuccess(); // 회원가입 성공 시 부모 컴포넌트에 알림
                // onClose(); // 모달 닫기 (바로 닫기보다 메시지 보여주는게 UX에 좋음)
                // 성공 메시지를 보여준 후 2초 뒤 모달 닫기
                setTimeout(() => {
                    onClose();
                }, 2000);

            } else { // 회원가입 실패 (백엔드 유효성 검사 실패, 아이디/이메일 중복 등)
                console.error('회원가입 실패:', data.message || '알 수 없는 오류');
                setErrorMessage(data.message || '회원가입에 실패했습니다. 다시 시도해주세요.');
            }

        } catch (error) {
            console.error('회원가입 중 네트워크 오류 발생:', error);
            setErrorMessage('서버와 통신 중 오류가 발생했습니다. 네트워크 상태를 확인해주세요.');
        } finally {
            setIsSigningUp(false); // 회원가입 진행 완료
        }
    };

    return (
        <ModalOverlay onClick={onClose}>
            <ModalContent onClick={e => e.stopPropagation()}>
                <CloseButton onClick={onClose} disabled={isSigningUp}>&times;</CloseButton>

                <ModalTitle>회원가입</ModalTitle>

                <SignupForm onSubmit={handleSubmit}>
                    <InputField>
                        <Label htmlFor="username">아이디</Label>
                        <Input
                            type="text"
                            id="username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            placeholder="4~20자 영문, 숫자"
                            disabled={isSigningUp}
                        />
                    </InputField>

                    <InputField>
                        <Label htmlFor="password">비밀번호</Label>
                        <Input
                            type="password"
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="8~30자 영문, 숫자, 특수문자"
                            disabled={isSigningUp}
                        />
                    </InputField>

                    <InputField>
                        <Label htmlFor="passwordConfirm">비밀번호 확인</Label>
                        <Input
                            type="password"
                            id="passwordConfirm"
                            value={passwordConfirm}
                            onChange={(e) => setPasswordConfirm(e.target.value)}
                            placeholder="비밀번호를 다시 입력하세요"
                            disabled={isSigningUp}
                        />
                    </InputField>

                    <InputField>
                        <Label htmlFor="email">이메일</Label>
                        <Input
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="예: example@email.com"
                            disabled={isSigningUp}
                        />
                    </InputField>

                    {errorMessage && <ErrorMessage>{errorMessage}</ErrorMessage>}
                    {successMessage && <SuccessMessage>{successMessage}</SuccessMessage>}

                    <SubmitButton type="submit" disabled={isSigningUp}>
                        {isSigningUp ? '가입 중...' : '회원가입'}
                    </SubmitButton>
                </SignupForm>

                {/* 이미 계정이 있다면 로그인으로 이동 */}
                <p style={{ textAlign: 'center', fontSize: '14px', color: '#666' }}>
                    이미 계정이 있으신가요? <a href="#" onClick={() => { onClose(); /* 로그인 모달 열기 함수 호출 등 */ }} style={{ color: '#007bff', textDecoration: 'none' }}>로그인</a>
                </p>
            </ModalContent>
        </ModalOverlay>
    );
};

export default SignupModal;