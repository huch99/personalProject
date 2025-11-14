import React, { useCallback, useEffect, useState } from 'react';
import { FaStar } from 'react-icons/fa';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import styled from 'styled-components';
import { checkSingleFavoriteStatus } from '../../features/tenders/tenderSlicce';

const DetailPageContainer = styled.div`
  max-width: 900px;
  margin: 40px auto;
  padding: 30px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
  position: relative;
`;

const DetailTitle = styled.h2`
  color: #2c3e50;
  margin-bottom: 25px;
  border-bottom: 3px solid #3498db;
  padding-bottom: 15px;
  font-size: 32px;
  text-align: center;
`;

const DetailSection = styled.div`
  margin-bottom: 20px;
  padding: 15px 0;
  border-bottom: 1px dashed #e0e0e0;

  &:last-child {
    border-bottom: none;
  }
`;

const DetailLabel = styled.span`
  font-weight: bold;
  color: #34495e;
  display: inline-block;
  width: 120px; /* 라벨 너비 고정 */
  margin-right: 15px;
`;

const DetailValue = styled.span`
  color: #555;
  font-size: 16px;
`;

const GoodsDescription = styled.div`
  background-color: #f8fafd;
  border: 1px solid #dbe9f7;
  border-radius: 8px;
  padding: 20px;
  margin-top: 25px;

  h3 {
    color: #3498db;
    margin-bottom: 15px;
    font-size: 22px;
  }

  p {
    white-space: pre-wrap; /* 줄 바꿈 유지 */
    color: #666;
    line-height: 1.8;
  }
`;

const BackButton = styled.button`
  display: block;
  width: 150px;
  margin: 30px auto 0;
  padding: 12px 20px;
  background-color: #6c757d;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 18px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #5a6268;
  }
`;

const LoadingMessage = styled.p`
  text-align: center;
  font-size: 18px;
  color: #3498db;
  margin-top: 50px;
`;

const ErrorMessage = styled.p`
  text-align: center;
  font-size: 18px;
  color: #e74c3c;
  margin-top: 50px;
`;

const FavoriteIcon = styled.div`
  position: absolute;
  top: 10px;
  right: 10px;
  cursor: pointer;
  color: ${props => props.isFavorite ? '#FFD700' : '#cccccc'}; // 노란색 or 회색
  font-size: 32px; // 목록보다 크게
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.1);
  }
`;

const TenderDetailPage = () => {
  const { cltrMnmtNo } = useParams(); // URL 파라미터에서 tenderId를 가져옴
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const favoriteTenderIds = useSelector(state => state.tenders.favoriteTenderIds);

  const [tenderDetail, setTenderDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const isCurrentTenderFavorite = favoriteTenderIds.includes(cltrMnmtNo);

  useEffect(() => {
    const fetchTenderDetail = async () => {
      try {
        setLoading(true);
        setError(null);

        const response = await fetch(`http://localhost:8080/api/tenders/${cltrMnmtNo}`);

        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(`HTTP error! status: ${response.status} - ${errorText}`);
        }

        const data = await response.json();
        setTenderDetail(data); // 백엔드에서 받은 TenderResponseDTO 데이터를 설정
      } catch (err) {
        setError(err.message || '물건 상세 정보를 불러오는데 실패했습니다.');
        console.error("Failed to fetch tender detail:", err);
      } finally {
        setLoading(false);
      }
    };

    if (cltrMnmtNo) {
      fetchTenderDetail();
      dispatch(checkSingleFavoriteStatus(cltrMnmtNo));
    }
  }, [cltrMnmtNo, dispatch]);

  // ✅ 즐겨찾기 토글 핸들러
  const handleToggleFavorite = useCallback(() => {
    dispatch(toggleFavorite({ cltrMnmtNo: cltrMnmtNo, isFavorite: isCurrentTenderFavorite }));
  }, [dispatch, cltrMnmtNo, isCurrentTenderFavorite]);


  if (loading) return <LoadingMessage>상세 정보 로딩 중...</LoadingMessage>;
  if (error) return <ErrorMessage>오류: {error}</ErrorMessage>;
  if (!tenderDetail) return <ErrorMessage>물건 정보를 찾을 수 없습니다.</ErrorMessage>;

  // 날짜/시간 포맷팅 헬퍼 함수
  const formatDateTime = (dt) => {
    if (!dt) return 'N/A';
    try {
      const date = new Date(dt); // 백엔드에서 LocalDateTime이 문자열로 오면 Date 객체로 변환
      return date.toLocaleString();
    } catch (e) {
      return dt; // 파싱 실패 시 원본 문자열 반환
    }
  };


  return (
    <DetailPageContainer>
      <DetailTitle>{tenderDetail.tenderTitle}</DetailTitle>

      <FavoriteIcon 
        isFavorite={isCurrentTenderFavorite} 
        onClick={handleToggleFavorite}
      >
        <FaStar />
      </FavoriteIcon>

      <DetailSection>
        <DetailLabel>공고번호:</DetailLabel>
        <DetailValue>{tenderDetail.pbctNo}</DetailValue>
      </DetailSection>

      <DetailSection>
        <DetailLabel>물건관리번호:</DetailLabel>
        <DetailValue>{tenderDetail.cltrMnmtNo}</DetailValue>
      </DetailSection>

      <DetailSection>
        <DetailLabel>처분방식:</DetailLabel>
        <DetailValue>{tenderDetail.organization}</DetailValue>
      </DetailSection>

      <DetailSection>
        <DetailLabel>입찰번호:</DetailLabel>
        <DetailValue>{tenderDetail.bidNumber}</DetailValue>
      </DetailSection>

      <DetailSection>
        <DetailLabel>공고일:</DetailLabel>
        <DetailValue>{formatDateTime(tenderDetail.announcementDate)}</DetailValue>
      </DetailSection>

      <DetailSection>
        <DetailLabel>입찰 마감일:</DetailLabel>
        <DetailValue>{formatDateTime(tenderDetail.deadline)}</DetailValue>
      </DetailSection>

      {/* 백엔드에서 GOODS_NM 필드를 가져와야 합니다. */}
      {/* 현재 TenderResponseDTO에 GOODS_NM은 없으므로, 백엔드에서 이 필드를 추가하고 파싱해야 함 */}
      {/* {tenderDetail.goodsName && ( // goodsName이라는 필드가 있다고 가정
                <GoodsDescription>
                    <h3>물건 상세 설명</h3>
                    <p>{tenderDetail.goodsName}</p>
                </GoodsDescription>
            )} */}

      <BackButton onClick={() => navigate(-1)}>뒤로 가기</BackButton>
    </DetailPageContainer>
  );
};

export default TenderDetailPage;