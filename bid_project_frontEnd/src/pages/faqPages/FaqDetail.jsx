import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import styled from 'styled-components';

const FaqDetailContainer = styled.div`
  padding: 20px;
  max-width: 900px;
  margin: 20px auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.08);
`;

const FaqHeader = styled.div`
  border-bottom: 2px solid #eee;
  padding-bottom: 15px;
  margin-bottom: 20px;
`;

const FaqTitle = styled.h2`
  color: #333;
  margin-bottom: 10px;
`;

const FaqMeta = styled.div`
  font-size: 14px;
  color: #777;
  display: flex;
  justify-content: space-between;
`;

const FaqContent = styled.div`
  font-size: 16px;
  line-height: 1.6;
  color: #444;
  white-space: pre-wrap; // 줄바꿈과 공백 유지
  min-height: 200px;
`;

const ButtonGroup = styled.div`
  display: flex;
  justify-content: flex-end;
  margin-top: 30px;
  gap: 10px;
`;

const ActionButton = styled.button`
  background-color: ${(props) => (props.$danger ? '#dc3545' : '#007bff')};
  color: white;
  padding: 8px 15px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 15px;

  &:hover {
    background-color: ${(props) => (props.$danger ? '#c82333' : '#0056b3')};
  }
`;

const BackToListButton = styled.button`
  background-color: #6c757d;
  color: white;
  padding: 8px 15px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 15px;
  margin-right: auto; // 왼쪽에 배치

  &:hover {
    background-color: #5a6268;
  }
`;

const FaqDetail = () => {
    const { faqId } = useParams();
    const navigate = useNavigate();
    const isLoggedIn = useSelector(state => state.login.isLoggedIn);
    const currentUserId = useSelector(state => state.login.userId);
    const usernameFromStore = useSelector(state => state.login.username);
    

    const [faq, setFaq] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

       

    useEffect(() => {
        // 로그인 체크 (프론트엔드 라우팅 보호 외에, 컴포넌트 내에서도 확인)
        if (!isLoggedIn) {
            alert('로그인한 사용자만 게시글을 조회할 수 있습니다.');
            navigate('/login'); // 로그인 페이지로 리다이렉트 (추후 LoginModal 띄우는 것으로 변경 가능)
            return;
        }

        const fetchFaqDetail = async () => {
            try {
                const accessToken = localStorage.getItem('accessToken'); // 토큰 가져오기
                const response = await fetch(`http://localhost:8080/api/faq/${faqId}`, {
                    headers: {
                        'Authorization': `Bearer ${accessToken}`, // JWT 토큰을 헤더에 포함
                        'Content-Type': 'application/json'
                    }
                });

                if (response.status === 401 || response.status === 403) {
                    alert('게시글을 조회할 권한이 없거나, 로그인이 필요합니다.');
                    navigate('/login');
                    return;
                }
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                const data = await response.json();
                setFaq(data);
            } catch (err) {
                setError(err.message);
                console.error("Failed to fetch FAQ detail:", err);
            } finally {
                setLoading(false);
            }
        };

        fetchFaqDetail();
    }, [faqId, isLoggedIn, navigate]);

    if (loading) return <FaqDetailContainer><p>로딩 중...</p></FaqDetailContainer>;
    if (error) return <FaqDetailContainer><p>오류 발생: {error}</p></FaqDetailContainer>;
    if (!faq) return <FaqDetailContainer><p>게시글을 찾을 수 없습니다.</p></FaqDetailContainer>;


    const isAuthor = faq.authorUsername === usernameFromStore; 
    
    const handleDelete = async () => {
        if (!window.confirm('정말로 이 게시글을 삭제하시겠습니까?')) return;

        try {
            const accessToken = localStorage.getItem('accessToken');
            const response = await fetch(`http://localhost:8080/api/faq/${faqId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    alert('게시글을 삭제할 권한이 없습니다.');
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            alert('게시글이 성공적으로 삭제되었습니다.');
            navigate('/faq'); // 목록 페이지로 이동
        } catch (err) {
            setError(err.message);
            alert('삭제 중 오류가 발생했습니다.');
            console.error("Failed to delete FAQ:", err);
        }
    };

    const handleEdit = () => {
        navigate(`/faq/${faqId}/edit`); // 수정 페이지로 이동
    };

    return (
        <FaqDetailContainer>
            <FaqHeader>
                <FaqTitle>{faq.title}</FaqTitle>
                <FaqMeta>
                    <span>작성자: {faq.authorUsername}</span>
                    <span>작성일: {new Date(faq.createdAt).toLocaleDateString()}</span>
                </FaqMeta>
            </FaqHeader>
            <FaqContent>{faq.content}</FaqContent>

            <ButtonGroup>
                <BackToListButton onClick={() => navigate('/faq')}>목록으로</BackToListButton>
                {isAuthor && ( // 작성자에게만 수정/삭제 버튼 표시
                    <>
                        <ActionButton onClick={handleEdit}>수정</ActionButton>
                        <ActionButton $danger onClick={handleDelete}>삭제</ActionButton>
                    </>
                )}
            </ButtonGroup>
        </FaqDetailContainer>
    );
};

export default FaqDetail;