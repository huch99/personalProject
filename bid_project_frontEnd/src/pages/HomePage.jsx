import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import styled from 'styled-components';
import { fetchTenders } from '../features/tenders/tenderSlicce';
import { Link, useNavigate } from 'react-router-dom';

// 스타일드 컴포넌트 정의
const S_HomePageContainer = styled.div`
  display: flex;
  flex-direction: column;
  gap: 40px; /* 섹션 간 간격 */
`;

const S_HeroSection = styled.section`
  background: linear-gradient(135deg, #2a9d8f 0%, #264653 100%); /* 그라데이션 배경 */
  color: white;
  padding: 60px 40px;
  text-align: center;
  border-radius: 12px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
`;

const S_HeroTitle = styled.h2`
  font-size: 3em;
  margin-bottom: 20px;
  font-weight: 700;
  letter-spacing: -1px;
`;

const S_HeroDescription = styled.p`
  font-size: 1.2em;
  line-height: 1.6;
  margin-bottom: 30px;
  opacity: 0.9;
`;

const S_CtaGroup = styled.div`
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
`;

const S_CtaButton = styled(Link)`
  background-color: #e9c46a; /* 강조 색상 */
  color: #264653;
  padding: 15px 30px;
  border-radius: 8px;
  text-decoration: none;
  font-size: 1.1em;
  font-weight: bold;
  transition: all 0.3s ease;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);

  &:hover {
    background-color: #f4a261;
    transform: translateY(-3px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2);
  }
`;

const S_BidListSection = styled.section`
  background-color: #ffffff;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
`;

const S_SectionTitle = styled.h3`
  font-size: 2.2em;
  color: #264653;
  margin-bottom: 30px;
  text-align: center;
  position: relative;

  &::after {
    content: '';
    display: block;
    width: 60px;
    height: 4px;
    background-color: #e76f51; /* 포인트 색상 */
    margin: 15px auto 0;
    border-radius: 2px;
  }
`;

const S_BidGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); /* 반응형 그리드 */
  gap: 25px;
`;

const S_BidItemCard = styled(Link)`
  display: flex;
  flex-direction: column;
  background-color: #f9f9f9;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 20px;
  text-decoration: none;
  color: inherit;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.1);
  }
`;

const S_BidTitle = styled.h4`
  font-size: 1.3em;
  color: #2a9d8f;
  margin-bottom: 10px;
  word-break: keep-all; /* 단어가 잘리지 않고 줄바꿈 */
  line-height: 1.4;
`;

const S_BidMeta = styled.p`
  font-size: 0.95em;
  color: #6c757d;
  margin-bottom: 5px;

  strong {
    color: #495057;
  }
`;

const S_LoadingText = styled.p`
  text-align: center;
  font-size: 1.2em;
  color: #264653;
  padding: 50px;
`;

const S_ErrorText = styled.p`
  text-align: center;
  font-size: 1.2em;
  color: #dc3545;
  padding: 50px;
  font-weight: bold;
`;

const HomePage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const bids = useSelector((state) => state.tenders.bids);
  const status = useSelector((state) => state.tenders.status);
  const error = useSelector((state) => state.tenders.error);
  const currentPage = useSelector((state) => state.tenders.currentPage); // ✅ Redux에서 현재 페이지
  const totalCount = useSelector((state) => state.tenders.totalCount);   // ✅ Redux에서 전체 아이템 수
  const numOfRows = useSelector((state) => state.tenders.numOfRows);     // ✅ Redux에서 한 페이지당 아이템 수

  useEffect(() => {
    dispatch(fetchTenders({ pageNo: currentPage })); 
  }, [dispatch, currentPage]);

  let content;

  if (status === 'loading') {
    content = <S_LoadingText>입찰 정보를 불러오는 중입니다...</S_LoadingText>;
  } else if (status === 'succeeded') {
    if (bids.length > 0) {
      console.log(bids);
      content = (
        <S_BidGrid>
          {bids.map((bid) => (
            <S_BidItemCard key={bid.cltrMnmtNo} to={`/tenders/${bid.cltrMnmtNo}`}>
              <S_BidTitle>{bid.tenderTitle}</S_BidTitle>
              <S_BidMeta>
                <strong>공고 번호 : </strong> {bid.cltrMnmtNo}
              </S_BidMeta>
              <S_BidMeta>
                <strong>처분 방식 : </strong> {bid.organization}
              </S_BidMeta>              
              <S_BidMeta>
                <strong>공고일 : </strong> {bid.announcementDate ? new Date(bid.announcementDate).toLocaleString() : 'N/A'}
              </S_BidMeta>
              <S_BidMeta>
                <strong>마감일 : </strong> {bid.deadline ? new Date(bid.deadline).toLocaleString() : 'N/A'}
              </S_BidMeta>
            </S_BidItemCard>
          ))}
        </S_BidGrid>
      );
    } else {
      content = <S_LoadingText>등록된 입찰 정보가 없습니다.</S_LoadingText>;
    }
  } else if (status === 'failed') {
    content = <S_ErrorText>{error}</S_ErrorText>;
  }

  // ✅ 페이징 관련 계산 (DetailedSearchPage와 동일)
  const totalPages = Math.ceil(totalCount / numOfRows);
  const pageNumbers = [];
  const maxPageButtons = 10; // 화면에 표시할 페이지 버튼 수
  let startPage = Math.max(1, currentPage - Math.floor(maxPageButtons / 2));
  let endPage = Math.min(totalPages, startPage + maxPageButtons - 1);

  // 현재 페이지가 끝 페이지에 가까울 때 startPage 조정
  if (endPage - startPage + 1 < maxPageButtons) {
    startPage = Math.max(1, endPage - maxPageButtons + 1);
  }

  for (let i = startPage; i <= endPage; i++) {
    pageNumbers.push(i);
  }

  // ✅ 페이지 변경 핸들러
  const handlePageChange = (page) => {
    if (page < 1 || page > totalPages) return;
    dispatch(setCurrentPageRedux(page)); // Redux 스토어에 페이지 변경 요청
  };

  return (
    <S_HomePageContainer>
      {/* <S_HeroSection>
        <S_HeroTitle>KAMCO 입찰 정보, 빠르고 정확하게!</S_HeroTitle>
        <S_HeroDescription>
          국내 공공 입찰 정보를 한눈에 확인하고,
          <br />
          간편하게 나에게 맞는 입찰을 찾아보세요.
        </S_HeroDescription>
        <S_CtaGroup>
          <S_CtaButton to="#">로그인</S_CtaButton>
          <S_CtaButton to="/signup">회원가입</S_CtaButton>
        </S_CtaGroup>
      </S_HeroSection> */}

      <S_BidListSection>
        <S_SectionTitle>최신 입찰 공고</S_SectionTitle>
        {content} {/* 위에서 조건부로 생성된 콘텐츠를 렌더링 */}
        {/* ✅ 페이지네이션 UI 추가 (DetailedSearchPage와 유사) */}
        {!status === 'loading' && !error && totalPages > 1 && (
          <S_PaginationContainer>
            <S_PageButton onClick={() => handlePageChange(1)} disabled={currentPage === 1}>
              {'<<'}
            </S_PageButton>
            <S_PageButton onClick={() => handlePageChange(currentPage - 1)} disabled={currentPage === 1}>
              {'<'}
            </S_PageButton>
            {pageNumbers.map((page) => (
              <S_PageButton
                key={page}
                onClick={() => handlePageChange(page)}
                active={page === currentPage}
              >
                {page}
              </S_PageButton>
            ))}
            <S_PageButton onClick={() => handlePageChange(currentPage + 1)} disabled={currentPage === totalPages}>
              {'>'}
            </S_PageButton>
            <S_PageButton onClick={() => handlePageChange(totalPages)} disabled={currentPage === totalPages}>
              {'>>'}
            </S_PageButton>
          </S_PaginationContainer>
        )}
      </S_BidListSection>
    </S_HomePageContainer>
  );
};

export default HomePage;