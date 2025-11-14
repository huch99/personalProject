import React, { useState } from 'react';
import { useDispatch } from 'react-redux';
import styled from 'styled-components';
import { loginSuccess } from '../../features/login_signup/loginSlice';

// 모달 외부 배경 (클릭 시 닫히도록)
const ModalOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7); /* 반투명 검은색 오버레이 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000; /* 다른 요소 위에 표시 */
`;

// 모달 내용 컨테이너
const ModalContent = styled.div`
  position: relative; /* CloseButton의 absolute 위치를 위한 기준점 */
  background-color: white;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 400px; /* 모달 너비 */
  max-width: 90%; /* 화면 크기에 따른 최대 너비 */
  z-index: 1001;
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

// 모달 제목 (Huch님이 분리를 원하셨지만, 현재는 한 파일에 통합)
const ModalTitle = styled.h2`
  margin-bottom: 20px;
  color: #333;
  text-align: center;
`;

// 로그인 폼
const LoginForm = styled.form`
  display: flex;
  flex-direction: column;
  gap: 15px;
`;

// 입력 필드를 감싸는 그룹 (InputField로 명명)
const InputField = styled.div`
  display: flex;
  flex-direction: column;
`;

// 라벨 스타일
const Label = styled.label`
  margin-bottom: 5px;
  font-weight: bold;
  color: #555;
`;

// 입력 필드 스타일 (HTML <input> 태그에 직접 매핑)
const Input = styled.input`
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;

  &:focus {
    border-color: #007bff;
    outline: none;
    box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
  }
`;

// 로그인 버튼
const SubmitButton = styled.button`
  padding: 12px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.3s ease;

  &:hover {
    background-color: #0056b3;
  }
`;

// 에러 메시지 스타일
const ErrorMessage = styled.p`
  color: red;
  text-align: center;
  margin-top: -10px; /* 폼 요소들과의 간격 조정 */
  font-size: 14px;
`;

// 모달 닫기 버튼 (모달 내용 안에)
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

const LoginModal = ({ isOpen, onClose }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const dispatch = useDispatch();

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault(); // 폼의 기본 제출(페이지 새로고침) 동작 방지

    // 1. 클라이언트 측 유효성 검사
    if (!username.trim() || !password.trim()) { // trim()으로 공백만 있는 경우도 체크
      setErrorMessage('아이디와 비밀번호를 모두 입력해주세요.');
      return;
    }

    setErrorMessage(''); // 에러 메시지 초기화

    // 2. 백엔드 로그인 API 호출
    try {
      const response = await fetch('http://localhost:8080/api/login', { // TODO: 여기에 Huch님의 실제 로그인 API 엔드포인트 URL을 입력해주세요.
        method: 'POST',
        headers: {
          'Content-Type': 'application/json', // JSON 형식으로 데이터 전송
        },
        body: JSON.stringify({ username, password }), // username과 password를 JSON 문자열로 변환
      });

      // 3. 백엔드 응답 처리
      const data = await response.json(); // 백엔드 응답이 JSON 형식이라고 가정

      if (response.ok && data.accessToken) {
        console.log("로그인 성공! 받은 Access Token:", data.accessToken);
        dispatch(loginSuccess({
          accessToken: data.accessToken,
          username: data.username,
          userId: data.userId,
          email: data.email
        }));

        onClose();

        window.location.reload();
      } else {
        console.error('로그인 실패 응답:', data);
        setErrorMessage(data.message || '아이디 또는 비밀번호가 올바르지 않습니다.');
      }

    } catch (error) {
      // 4. 네트워크 통신 중 에러 발생 (서버에 요청조차 도달하지 못한 경우 등)
      console.error('로그인 중 네트워크 오류 발생:', error);
      setErrorMessage('서버와 통신 중 오류가 발생했습니다. 네트워크 상태를 확인해주세요.');
    }
  };

  return (
    <ModalOverlay onClick={onClose}> {/* 오버레이 클릭 시 모달 닫기 */}
      <ModalContent onClick={e => e.stopPropagation()}> {/* 모달 내용 클릭 시 닫히지 않도록 */}
        <CloseButton onClick={onClose}>&times;</CloseButton> {/* 닫기 버튼 */}

        <ModalTitle>로그인</ModalTitle> {/* styled-component로 정의된 제목 */}

        <LoginForm onSubmit={handleSubmit}>
          <InputField>
            <Label htmlFor="username">아이디</Label>
            <Input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="아이디를 입력하세요"
            />
          </InputField>

          <InputField>
            <Label htmlFor="password">비밀번호</Label>
            <Input
              type="password" // 비밀번호는 type="password"로 설정
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력하세요"
            />
          </InputField>

          {errorMessage && <ErrorMessage>{errorMessage}</ErrorMessage>} {/* 에러 메시지 표시 */}

          <SubmitButton type="submit">로그인</SubmitButton>
        </LoginForm>

        {/* 회원가입 및 기타 링크 */}
        <p style={{ textAlign: 'center', fontSize: '14px', color: '#666' }}>
          아직 회원이 아니신가요? <a href="/signup" style={{ color: '#007bff', textDecoration: 'none' }}>회원가입</a>
        </p>
      </ModalContent>
    </ModalOverlay>
  );
};

export default LoginModal;