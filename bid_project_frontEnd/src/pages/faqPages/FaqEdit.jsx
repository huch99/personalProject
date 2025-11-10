import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import styled from 'styled-components';

const FaqEditContainer = styled.div`
  max-width: 800px;
  margin: 40px auto;
  padding: 25px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
`;

const FormTitle = styled.h2`
  color: #333;
  margin-bottom: 25px;
  border-bottom: 2px solid #eee;
  padding-bottom: 10px;
  font-size: 28px;
  text-align: center;
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
`;

const InputGroup = styled.div`
  margin-bottom: 20px;
`;

const Label = styled.label`
  display: block;
  margin-bottom: 8px;
  font-weight: bold;
  color: #555;
`;

const Input = styled.input`
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 16px;
  box-sizing: border-box;

  &:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
  }
`;

const TextArea = styled.textarea`
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 5px;
  font-size: 16px;
  min-height: 200px;
  box-sizing: border-box;
  resize: vertical;

  &:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 3px rgba(0, 123, 255, 0.25);
  }
`;

const ButtonGroup = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
`;

const SubmitButton = styled.button`
  background-color: #007bff;
  color: white;
  padding: 12px 25px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 18px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #0056b3;
  }

  &:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
  }
`;

const CancelButton = styled.button`
  background-color: #6c757d;
  color: white;
  padding: 12px 25px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 18px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #5a6268;
  }
`;

const ErrorMessage = styled.p`
  color: red;
  text-align: center;
  font-size: 14px;
  margin-top: 10px;
`;

const LoadingMessage = styled.p`
  text-align: center;
  font-size: 16px;
  color: #555;
`;

const FaqEdit = () => {
    const { faqId } = useParams();
    const navigate = useNavigate();
    const isLoggedIn = useSelector(state => state.login.isLoggedIn);
    const currentUsername = useSelector(state => state.login.username); // 현재 로그인된 사용자 이름
    const currentUserId = useSelector(state => state.login.userId); // 현재 로그인된 사용자 ID (백엔드 권한 확인용)

    const [title, setTitle] = useState('');
    const [content, setContent] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [faqAuthorId, setFaqAuthorId] = useState(null); // 게시글 작성자의 ID

    useEffect(() => {
        // 로그인 체크 및 토큰 유효성 확인
        if (!isLoggedIn) {
            alert('로그인이 필요한 서비스입니다.');
            navigate('/login');
            return;
        }

        const fetchFaqData = async () => {
            try {
                const accessToken = localStorage.getItem('accessToken');
                const response = await fetch(`http://localhost:8080/api/faq/${faqId}`, {
                    headers: {
                        'Authorization': `Bearer ${accessToken}`
                    }
                });

                if (response.status === 401 || response.status === 403) {
                    alert('게시글을 수정할 권한이 없거나, 로그인이 필요합니다.');
                    navigate('/login');
                    return;
                }
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }

                const data = await response.json();
                setTitle(data.title);
                setContent(data.content);
                setFaqAuthorId(data.authorId); // 게시글 작성자 ID 저장
                // 여기서 data.authorUsername도 함께 확인하여 currentUsername과 비교할 수도 있습니다.

            } catch (err) {
                setError(err.message);
                console.error("Failed to fetch FAQ:", err);
            } finally {
                setLoading(false);
            }
        };

        fetchFaqData();
    }, [faqId, navigate, isLoggedIn]);

    // 게시글 작성자 ID 로드 완료 후, 현재 사용자 ID와 비교
    useEffect(() => {
        if (loading === false && faqAuthorId !== null && currentUserId !== null) {
            if (Number(faqAuthorId) !== Number(currentUserId)) {
                alert('이 게시글을 수정할 권한이 없습니다.');
                navigate(`/faq/${faqId}`); // 상세 페이지로 돌려보냄
            }
        }
    }, [loading, faqAuthorId, currentUserId, navigate, faqId]);


    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null); // 이전 에러 메시지 초기화

        // 기본 유효성 검사
        if (!title.trim() || !content.trim()) {
            setError('제목과 내용은 비워둘 수 없습니다.');
            return;
        }

        try {
            const accessToken = localStorage.getItem('accessToken');
            const response = await fetch(`http://localhost:8080/api/faq/${faqId}`, {
                method: 'PUT', // ✅ PUT 요청
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${accessToken}`
                },
                body: JSON.stringify({ title, content })
            });

            if (response.status === 401 || response.status === 403) {
                alert('게시글 수정 권한이 없거나, 로그인이 필요합니다.');
                navigate('/login');
                return;
            }

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
            }

            alert('게시글이 성공적으로 수정되었습니다.');
            navigate(`/faq/${faqId}`); // 수정 후 상세 페이지로 이동
        } catch (err) {
            setError(err.message);
            console.error("Failed to update FAQ:", err);
        }
    };

    const handleCancel = () => {
        navigate(`/faq/${faqId}`); // 수정 취소 시 상세 페이지로 이동
    };


    if (loading) return <FaqEditContainer><LoadingMessage>게시글 로딩 중...</LoadingMessage></FaqEditContainer>;
    if (error) return <FaqEditContainer><ErrorMessage>오류 발생: {error}</ErrorMessage></FaqEditContainer>;


    return (
         <FaqEditContainer>
            <FormTitle>FAQ 수정</FormTitle>
            <Form onSubmit={handleSubmit}>
                <InputGroup>
                    <Label htmlFor="title">제목</Label>
                    <Input
                        type="text"
                        id="title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        placeholder="제목을 입력하세요."
                        required
                    />
                </InputGroup>
                <InputGroup>
                    <Label htmlFor="content">내용</Label>
                    <TextArea
                        id="content"
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        placeholder="내용을 입력하세요."
                        required
                    />
                </InputGroup>
                {error && <ErrorMessage>{error}</ErrorMessage>}
                <ButtonGroup>
                    <CancelButton type="button" onClick={handleCancel}>취소</CancelButton>
                    <SubmitButton type="submit">수정 완료</SubmitButton>
                </ButtonGroup>
            </Form>
        </FaqEditContainer>
    );
};

export default FaqEdit;