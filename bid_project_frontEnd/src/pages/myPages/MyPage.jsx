import React, { useCallback, useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { updateProfileSuccess } from '../../features/login_signup/loginSlice';
import EmailEditModal from '../../components/modal/EmailEditModal';
import PasswordChangeModal from '../../components/modal/PasswordChangeModal';
import { fetchFavoriteTenderIds, toggleFavorite } from '../../features/tenders/tenderSlicce';
import axios from 'axios';
import { FaStar } from 'react-icons/fa';

const MyPageContainer = styled.div`
  max-width: 800px;
  margin: 50px auto;
  padding: 30px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.1);
`;

const PageTitle = styled.h2`
  color: #2c3e50;
  margin-bottom: 30px;
  border-bottom: 3px solid #3498db;
  padding-bottom: 15px;
  font-size: 32px;
  text-align: center;
`;

const Section = styled.section`
  background-color: #f8fafd;
  border: 1px solid #dbe9f7;
  border-radius: 8px;
  padding: 25px;
  margin-bottom: 30px;
`;

const SectionTitle = styled.h3`
  color: #3498db;
  font-size: 24px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e0e0e0;
  padding-bottom: 10px;
`;

const InfoGroup = styled.div`
  margin-bottom: 15px;
  display: flex;
  align-items: center;
`;

const Label = styled.span`
  font-weight: bold;
  color: #34495e;
  width: 120px;
  flex-shrink: 0;
`;

const Value = styled.span`
  color: #555;
  flex-grow: 1;
`;

const Input = styled.input`
  padding: 10px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 16px;
  flex-grow: 1;
  margin-left: 10px;

  &:focus {
    outline: none;
    border-color: #007bff;
    box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
  }
`;

const Button = styled.button`
  background-color: #007bff;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.2s ease;
  margin-top: 15px;
  margin-left: 10px;

  &:hover {
    background-color: #0056b3;
  }

  &:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
  }
`;

const ButtonGroup = styled.div`
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
`;

const ErrorMessage = styled.p`
  color: red;
  font-size: 14px;
  margin-top: 5px;
  text-align: right;
  margin-left: auto;
`;

const SuccessMessage = styled.p`
  color: green;
  font-size: 14px;
  margin-top: 5px;
  text-align: right;
  margin-left: auto;
`;

const FavoriteList = styled.ul`
  list-style: none;
  padding: 0;
`;

const FavoriteItem = styled.li`
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 6px;
  padding: 12px 15px;
  margin-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 15px;
  color: #333;
  position: relative;

  &:last-child {
    margin-bottom: 0;
  }
`;

const DeleteButton = styled.button`
  background-color: #dc3545;
  color: white;
  padding: 5px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: #c82333;
  }
`;

const ProfileEditButtonGroup = styled.div`
  display: flex;
  gap: 10px;
  margin-top: 20px;
  justify-content: flex-end; /* 오른쪽 정렬 */
`;

const FavoriteIcon = styled.div`
  position: absolute;
  top: 5px;
  right: 5px;
  cursor: pointer;
  color: #FFD700; // 마이페이지에서는 이미 즐겨찾기 된 것이므로 항상 노란색
  font-size: 24px;
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.1);
  }
`;

const MyPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const favoriteTenderIds = useSelector(state => state.tenders.favoriteTenderIds);
  const isFavoriteLoading = useSelector(state => state.tenders.isFavoriteLoading);
  const isFavoriteError = useSelector(state => state.tenders.isFavoriteError);

  const isLoggedIn = useSelector(state => state.login.isLoggedIn);
  const username = useSelector(state => state.login.username);
  const userId = useSelector(state => state.login.userId);
  const email = useSelector(state => state.login.email);

  const [loading, setLoading] = useState(true); // 실제 API 호출 없이 Redux에서 가져오므로 빠르게 false로 설정 가능
  const [profileError, setProfileError] = useState(null);

  const [showEmailModal, setShowEmailModal] = useState(false); // 이메일 모달 표시 여부
  const [showPasswordModal, setShowPasswordModal] = useState(false); // 비밀번호 모달 표시 여부

  const [favoriteTendersDetail, setFavoriteTendersDetail] = useState([]); // ✅ 즐겨찾기 상세 정보를 저장할 상태
  const [loadingFavoriteDetails, setLoadingFavoriteDetails] = useState(false); // ✅ 즐겨찾기 상세 정보 로딩
  const [errorFavoriteDetails, setErrorFavoriteDetails] = useState(null);

  useEffect(() => {
    if (!isLoggedIn) {
      navigate('/login');
    }
    setLoading(false);
  }, [isLoggedIn, navigate]);

  useEffect(() => {
    if (isLoggedIn) {
      dispatch(fetchFavoriteTenderIds());
    }
  }, [isLoggedIn, dispatch]);

  useEffect(() => {
    const fetchDetails = async () => {
      if (!isLoggedIn || favoriteTenderIds.length === 0) {
        setFavoriteTendersDetail([]);
        return;
      }
      setLoadingFavoriteDetails(true);
      setErrorFavoriteDetails(null);
      try {
        const token = localStorage.getItem('accessToken');

        if (!token) {
          // 토큰이 없으면 로그인 페이지로 리다이렉트하거나 에러 처리
          console.warn("No access token found. Cannot fetch favorite details.");
          setErrorFavoriteDetails("로그인이 필요합니다.");
          setLoadingFavoriteDetails(false);
          return;
        }

        const response = await fetch('http://localhost:8080/api/favorites', {
          headers: {
            'Authorization': `Bearer ${token}` // 🌟 Authorization 헤더 추가
          }
        });

        if (!response.ok) {
          // HTTP 상태 코드가 2xx가 아닐 경우 에러 처리
          const errorBody = await response.text(); // 에러 메시지 확인을 위해 텍스트로 받기
          console.error(`HTTP error! status: ${response.status}, message: ${errorBody}`);
          throw new Error(`관심 입찰 정보를 불러오는 데 실패했습니다: ${response.status}`);
        }
        const data = await response.json(); // 응답 데이터를 JSON으로 파싱
        setFavoriteTendersDetail(data);
      } catch (err) {
        console.error("Error fetching favorite tenders details:", err);
        setErrorFavoriteDetails("관심 입찰 상세 정보를 불러오는 데 실패했습니다.");
      } finally {
        setLoadingFavoriteDetails(false);
      }
    };

    if (isLoggedIn && isFavoriteLoading === 'succeeded') {
      fetchDetails();
    } else if (!isLoggedIn) {
      setFavoriteTendersDetail([]); // 로그아웃 상태면 목록 비움
    }
  }, [isLoggedIn, favoriteTenderIds, isFavoriteLoading]);

  // ✅ 즐겨찾기 해제 핸들러
  const handleRemoveFavorite = useCallback((e, cltrMnmtNo) => {
    e.stopPropagation(); // ✅ 이벤트 버블링 방지 (아이템 클릭 -> 상세 페이지 이동 막기)
    dispatch(toggleFavorite({ cltrMnmtNo, isFavorite: true })); // isFavorite: true => 현재 즐겨찾기 되어있으니 해제 요청
  }, [dispatch]);

  // 로딩 및 에러 처리 (기존 구조를 최대한 유지)
  if (loading || !isLoggedIn || isFavoriteLoading === 'pending' || loadingFavoriteDetails) {
    return (
      <MyPageContainer>
        <p style={{ textAlign: 'center' }}>{
          loading ? '로그인 정보 확인 중...' :
            isFavoriteLoading === 'pending' || loadingFavoriteDetails ? '관심 입찰 목록 로딩 중...' :
              '불러오는 중...'
        }</p>
      </MyPageContainer>
    );
  }

  // 최종 에러 메시지
  if (profileError || isFavoriteError || errorFavoriteDetails) {
    return (
      <MyPageContainer>
        <p style={{ textAlign: 'center', color: 'red' }}>
          {profileError || isFavoriteError || errorFavoriteDetails}
        </p>
      </MyPageContainer>
    );
  }

  return (
    <MyPageContainer>
      <PageTitle>마이페이지</PageTitle>

      {/* 1. 회원 정보 조회 */}
      <Section>
        <SectionTitle>회원 정보</SectionTitle>
        <InfoGroup>
          <Label>사용자 ID:</Label>
          <Value>{username}</Value>
        </InfoGroup>
        <InfoGroup>
          <Label>이메일:</Label>
          <Value>{email}</Value>
        </InfoGroup>
        {/* 회원 정보 밑에 수정/변경 버튼들 추가 */}
        <ProfileEditButtonGroup>
          <Button onClick={() => setShowEmailModal(true)}>이메일 수정</Button>
          <Button onClick={() => setShowPasswordModal(true)} style={{ backgroundColor: '#6c757d' }}>비밀번호 변경</Button>
        </ProfileEditButtonGroup>
      </Section>

      {/* 이메일 수정 모달 */}
      {showEmailModal && (
        <EmailEditModal
          onClose={() => setShowEmailModal(false)}
          currentEmail={email} // 현재 이메일을 모달로 전달
          username={username} // 사용자 이름을 모달로 전달 (API 호출 시 필요)
        />
      )}

      {/* 비밀번호 변경 모달 */}
      {showPasswordModal && (
        <PasswordChangeModal
          onClose={() => setShowPasswordModal(false)}
        />
      )}

      {/* 4. 관심 입찰 목록 (TODO: 백엔드 및 프론트엔드 추가 구현 필요) */}
      <Section>
        <SectionTitle>관심 입찰 목록</SectionTitle>
        {favoriteTendersDetail.length === 0 ? ( // ✅ 변경: interestTenders -> favoriteTendersDetail
          <p style={{ textAlign: 'center', color: '#666' }}>관심 입찰이 없습니다. 원하는 입찰을 찾아 추가해보세요!</p>
        ) : (
          <FavoriteList> {/* ✅ 변경: InterestList -> FavoriteList */}
            {favoriteTendersDetail.map((item) => ( // ✅ 변경: interestTenders -> favoriteTendersDetail
              <FavoriteItem key={item.cltrMnmtNo} onClick={() => navigate(`/tenders/${item.cltrMnmtNo}`)}> {/* ✅ 변경: item.id -> item.cltrMnmtNo */}
                <h4 style={{ cursor: 'pointer' }}>{item.tenderTitle}</h4> {/* 클릭 가능하도록 */}
                <p><strong>공고번호:</strong> {item.pbctNo}</p>
                <p><strong>처분방식:</strong> {item.organization}</p>
                {/* 추가적인 정보를 표시할 수 있습니다 */}
                <FavoriteIcon
                  onClick={(e) => handleRemoveFavorite(e, item.cltrMnmtNo)}
                >
                  <FaStar />
                </FavoriteIcon>
              </FavoriteItem>
            ))}
          </FavoriteList>
        )}
      </Section>

      {/* 홈으로 버튼 */}
      <ButtonGroup>
        <Button onClick={() => navigate('/')}>홈으로</Button>
      </ButtonGroup>
    </MyPageContainer>
  );
};

export default MyPage;