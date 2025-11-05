import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';

const FaqWriteContainer = styled.div`
  padding: 20px;
  max-width: 900px;
  margin: 20px auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
`;

const Title = styled.h2`
  text-align: center;
  color: #333;
  margin-bottom: 30px;
`;

const FaqForm = styled.form`
  display: flex;
  flex-direction: column;
  gap: 15px;
`;

const Label = styled.label`
  font-weight: bold;
  color: #555;
`;

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

const TextArea = styled.textarea`
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  min-height: 200px;
  resize: vertical;

  &:focus {
    border-color: #007bff;
    outline: none;
    box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
  }
`;

const ButtonGroup = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
`;

const SubmitButton = styled.button`
  background-color: #28a745;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;

  &:hover {
    background-color: #218838;
  }
`;

const CancelButton = styled.button`
  background-color: #6c757d;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;

  &:hover {
    background-color: #5a6268;
  }
`;

const ErrorMessage = styled.p`
  color: red;
  font-size: 14px;
  margin-top: 10px;
`;

const FaqWrite = () => {
    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();
    const isLoggedIn = useSelector(state => state.login.isLoggedIn);

    useEffect(() => {
        // 로그인 체크 (프론트엔드 라우팅 보호 외에, 컴포넌트 내에서도 확인)
        if (!isLoggedIn) {
            alert('로그인한 사용자만 게시글을 작성할 수 있습니다.');
            navigate('/login'); // 로그인 페이지로 리다이렉트
        }
    }, [isLoggedIn, navigate]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (!title.trim() || !content.trim()) {
            setError('제목과 내용을 모두 입력해주세요.');
            return;
        }

        try {
            const accessToken = localStorage.getItem('accessToken');
            const response = await fetch('http://localhost:8080/api/faq', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${accessToken}`, // JWT 토큰을 헤더에 포함
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ title, content })
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    alert('글을 작성할 권한이 없거나, 로그인이 필요합니다.');
                    navigate('/login');
                    return;
                }
                const errorData = await response.json(); // 백엔드에서 에러 메시지 반환한다고 가정
                throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
            }

            alert('게시글이 성공적으로 작성되었습니다.');
            navigate('/faq'); // 목록 페이지로 이동
        } catch (err) {
            setError(err.message);
            console.error("Failed to create FAQ:", err);
        }
    };

    const handleCancel = () => {
        navigate('/faq'); // 목록 페이지로 이동
    };

    if (!isLoggedIn) return null;

    return (
        <FaqWriteContainer>
            <Title>FAQ 게시글 작성</Title>
            <FaqForm onSubmit={handleSubmit}>
                <Label htmlFor="title">제목</Label>
                <Input
                    id="title"
                    type="text"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="제목을 입력하세요"
                />
                <Label htmlFor="content">내용</Label>
                <TextArea
                    id="content"
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    placeholder="내용을 입력하세요"
                />
                {error && <ErrorMessage>{error}</ErrorMessage>}
                <ButtonGroup>
                    <CancelButton type="button" onClick={handleCancel}>취소</CancelButton>
                    <SubmitButton type="submit">작성 완료</SubmitButton>
                </ButtonGroup>
            </FaqForm>
        </FaqWriteContainer>
    );
};

export default FaqWrite;