import React, { useEffect, useState } from 'react';
import { useDispatch } from 'react-redux';
import styled from 'styled-components';
import { updateProfileSuccess } from '../../features/login_signup/loginSlice';

const ModalOverlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
`;

const ModalContent = styled.div`
  background-color: #fff;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  width: 90%;
  max-width: 500px;
  position: relative;
`;

const ModalTitle = styled.h3`
  color: #34495e;
  font-size: 24px;
  margin-bottom: 25px;
  text-align: center;
  border-bottom: 2px solid #eee;
  padding-bottom: 10px;
`;

const ModalLabel = styled.label`
  display: block;
  font-weight: bold;
  color: #555;
  margin-bottom: 8px;
  margin-top: 15px;
`;

const ModalInput = styled.input`
  width: calc(100% - 22px); /* Padding + Border 고려 */
  padding: 12px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 16px;

  &:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
  }
`;

const ModalButton = styled.button`
  background-color: #007bff;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.2s ease;
  margin-top: 20px;
  margin-right: 10px; /* 버튼 사이 간격 */

  &:hover {
    background-color: #0056b3;
  }

  &:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
  }
`;

const ModalCloseButton = styled.button`
  position: absolute;
  top: 15px;
  right: 15px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;

  &:hover {
    color: #333;
  }
`;

const ModalButtonGroup = styled.div`
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
`;

const ModalErrorMessage = styled.p`
  color: red;
  font-size: 14px;
  margin-top: 10px;
  text-align: center;
`;

const ModalSuccessMessage = styled.p`
  color: green;
  font-size: 14px;
  margin-top: 10px;
  text-align: center;
`;

const EmailEditModal = ({ onClose, currentEmail, username }) => {
    const dispatch = useDispatch();
    const [newEmail, setNewEmail] = useState(currentEmail || '');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    // currentEmail이 변경될 경우 newEmail도 업데이트 (마이페이지에서 새로 불러왔을 때 등)
    useEffect(() => {
        setNewEmail(currentEmail || '');
    }, [currentEmail]);

    const handleUpdateEmail = async () => {
        const accessToken = localStorage.getItem('accessToken');

        if (!accessToken) {
            setError('로그인이 필요합니다.');
            return;
        }
        if (!newEmail || !newEmail.includes('@')) {
            setError('유효한 이메일 주소를 입력해주세요.');
            return;
        }
        if (newEmail === currentEmail) {
            setError('현재 이메일과 동일합니다.');
            return;
        }

        setMessage('');
        setError('');

        try {
            const response = await fetch('http://localhost:8080/api/mypage/profile', {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email: newEmail, username: username }), // username도 함께 전달
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '이메일 수정에 실패했습니다.');
            }

            const updatedProfile = await response.json();
            dispatch(updateProfileSuccess(updatedProfile)); // Redux 스토어 업데이트
            setMessage('이메일이 성공적으로 수정되었습니다.');
        } catch (err) {
            console.error("Failed to update email:", err);
            setError(err.message || '이메일 수정 중 오류가 발생했습니다.');
        }
    };

    return (
        <ModalOverlay>
            <ModalContent>
                <ModalCloseButton onClick={onClose}>&times;</ModalCloseButton>
                <ModalTitle>이메일 수정</ModalTitle>
                <ModalLabel htmlFor="newEmail">새로운 이메일:</ModalLabel>
                <ModalInput
                    id="newEmail"
                    type="email"
                    value={newEmail}
                    onChange={(e) => setNewEmail(e.target.value)}
                    placeholder="새로운 이메일 주소를 입력하세요"
                />
                {error && <ModalErrorMessage>{error}</ModalErrorMessage>}
                {message && <ModalSuccessMessage>{message}</ModalSuccessMessage>}
                <ModalButtonGroup>
                    <ModalButton onClick={handleUpdateEmail} disabled={newEmail === currentEmail || !newEmail.includes('@')}>수정</ModalButton>
                    <ModalButton onClick={onClose} style={{ backgroundColor: '#6c757d' }}>닫기</ModalButton>
                </ModalButtonGroup>
            </ModalContent>
        </ModalOverlay>
    );
};

export default EmailEditModal;