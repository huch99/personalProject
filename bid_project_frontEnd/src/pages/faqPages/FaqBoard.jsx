import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import styled from 'styled-components';

const FaqBoardContainer = styled.div`
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

const FaqList = styled.ul`
  list-style: none;
  padding: 0;
`;

const FaqListItem = styled.li`
  border-bottom: 1px solid #eee;
  padding: 15px 0;
  &:last-child {
    border-bottom: none;
  }
`;

const FaqTitleLink = styled(Link)`
  font-size: 18px;
  color: #007bff;
  text-decoration: none;
  font-weight: bold;
  display: block; // 전체 영역 클릭 가능
  cursor: pointer;

  &:hover {
    text-decoration: underline;
    color: #0056b3;
  }
`;

const FaqMeta = styled.div`
  font-size: 13px;
  color: #666;
  margin-top: 5px;
  display: flex;
  justify-content: space-between;
`;

const WriteButtonContainer = styled.div`
  text-align: right;
  margin-top: 20px;
`;

const WriteButton = styled(Link)`
  display: inline-block;
  background-color: #28a745;
  color: white;
  padding: 10px 15px;
  border-radius: 5px;
  text-decoration: none;
  font-weight: bold;

  &:hover {
    background-color: #218838;
  }
`;

const NoFaqMessage = styled.p`
  text-align: center;
  color: #777;
  padding: 30px;
`;

const FaqBoard = () => {
  const [faqs, setFaqs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const isLoggedIn = useSelector(state => state.login.isLoggedIn);

  useEffect(() => {
    const fetchFaqs = async () => {
      try {
        const response = await fetch('http://localhost:8080/api/faq'); // 누구나 접근 가능
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        setFaqs(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchFaqs();
  }, []);

  if (loading) return <FaqBoardContainer><p>로딩 중...</p></FaqBoardContainer>;
  if (error) return <FaqBoardContainer><p>오류 발생: {error}</p></FaqBoardContainer>;

  const handleFaqClick = (e, faqId) => {
    if (!isLoggedIn) {
      e.preventDefault(); // ✅ 로그인 안 되어 있으면 Link의 기본 동작(페이지 이동)을 막습니다.
      alert('로그인한 사용자만 게시글을 조회할 수 있습니다.');
      navigate('/login');
    }
  };

  return (
    <FaqBoardContainer>
      <Title>자주 묻는 질문 (FAQ)</Title>
      {faqs.length === 0 ? (
        <NoFaqMessage>게시글이 없습니다. 첫 번째 게시글을 작성해보세요!</NoFaqMessage>
      ) : (
        <FaqList>
          {faqs.map((faq) => (
            <FaqListItem key={faq.faqId}>
              <FaqTitleLink to={`/faq/${faq.faqId}`} onClick={() => handleFaqClick(faq.faqId)}>
                {faq.title}
              </FaqTitleLink>
              <FaqMeta>
                <span>작성자: {faq.authorUsername}</span>
                <span>{new Date(faq.createdAt).toLocaleDateString()}</span>
              </FaqMeta>
            </FaqListItem>
          ))}
        </FaqList>
      )}

      {/* 로그인한 사용자만 글 작성 버튼 표시 */}
      {isLoggedIn && (
        <WriteButtonContainer>
          <WriteButton to="/faq/write">글 작성</WriteButton>
        </WriteButtonContainer>
      )}
    </FaqBoardContainer>
  );
};

export default FaqBoard;