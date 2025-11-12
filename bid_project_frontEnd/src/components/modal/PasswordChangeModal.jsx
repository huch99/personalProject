import React, { useState } from 'react';
import styled from 'styled-components';

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

const PasswordChangeModal = ({onClose}) => {
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const [message, setMessage] = useState('');
    const [error, setError] = useState('');

    const handleChangePassword = async () => {
        const accessToken = localStorage.getItem('accessToken');

        if (!accessToken) {
            setError('로그인이 필요합니다.');
            return;
        }

        setMessage('');
        setError('');

        if (newPassword !== confirmNewPassword) {
            setError('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.');
            return;
        }
        if (!currentPassword || !newPassword || !confirmNewPassword) {
            setError('모든 비밀번호 필드를 입력해주세요.');
            return;
        }
        if (currentPassword === newPassword) {
            setError('현재 비밀번호와 새 비밀번호가 동일합니다.');
            return;
        }

        try {
            const response = await fetch('http://localhost:8080/api/mypage/password', {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${accessToken}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    currentPassword,
                    newPassword
                }),
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || '비밀번호 변경에 실패했습니다.');
            }

            setMessage('비밀번호가 성공적으로 변경되었습니다. 다시 로그인해야 할 수도 있습니다.');
            // 변경 성공 후 필드 초기화
            setCurrentPassword('');
            setNewPassword('');
            setConfirmNewPassword('');
        } catch (err) {
            console.error("Failed to change password:", err);
            setError(err.message || '비밀번호 변경 중 오류가 발생했습니다.');
        }
    };

    return (
        <ModalOverlay>
            <ModalContent>
                <ModalCloseButton onClick={onClose}>&times;</ModalCloseButton>
                <ModalTitle>비밀번호 변경</ModalTitle>
                <ModalLabel htmlFor="currentPasswordModal">현재 비밀번호:</ModalLabel>
                <ModalInput
                    id="currentPasswordModal"
                    type="password"
                    value={currentPassword}
                    onChange={(e) => setCurrentPassword(e.target.value)}
                    placeholder="현재 비밀번호를 입력하세요"
                />
                <ModalLabel htmlFor="newPasswordModal">새 비밀번호:</ModalLabel>
                <ModalInput
                    id="newPasswordModal"
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="새 비밀번호를 입력하세요"
                />
                <ModalLabel htmlFor="confirmNewPasswordModal">새 비밀번호 확인:</ModalLabel>
                <ModalInput
                    id="confirmNewPasswordModal"
                    type="password"
                    value={confirmNewPassword}
                    onChange={(e) => setConfirmNewPassword(e.target.value)}
                    placeholder="새 비밀번호를 다시 입력하세요"
                />
                {error && <ModalErrorMessage>{error}</ModalErrorMessage>}
                {message && <ModalSuccessMessage>{message}</ModalSuccessMessage>}
                <ModalButtonGroup>
                    <ModalButton onClick={handleChangePassword}>변경</ModalButton>
                    <ModalButton onClick={onClose} style={{ backgroundColor: '#6c757d' }}>닫기</ModalButton>
                </ModalButtonGroup>
            </ModalContent>
        </ModalOverlay>
    );
};

export default PasswordChangeModal;