import React, { useState } from 'react';
import styled from 'styled-components';
import { Link } from 'react-router-dom';
import LoginModal from '../modal/LoginModal';
import SignupModal from '../modal/SignupModal';
import { useDispatch, useSelector } from 'react-redux';
import { logout } from '../../features/login_signup/loginSlice';
import media from '../../styles/media';

// Header styled components
const S_Header = styled.header`
  background-color: #283747; /* 다크 블루 계열 */
  color: white;
  padding: 15px 20px;
  border-bottom: 1px solid #1a242f;
`;

const S_HeaderInner = styled.div`
  display: flex;
  justify-content: flex-start;
  align-items: center;
  width: 100%; 

  // --- 모바일 (기본값) ---
  padding: 0; 

  // --- 태블릿 ---
  ${media.tablet`
    padding: 0 20px;     
  `}

  // --- 데스크탑 ---
  ${media.desktop`
   padding: 0 40px;
  `}
`;

const S_SiteTitleContainer = styled.div`
  display: flex;
  align-items: center;
  margin-right: auto;
`;

const S_SiteTitle = styled.h1`
  margin: 0;
  font-size: 24px;

  & > a {
    color: white;
    text-decoration: none;
  }
`;

const S_MainNav = styled.nav`
  ul {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    margin-left: auto; 
    align-items: center;
  }

  li {
    margin-left: 20px;
    white-space: nowrap;
  }

  a {
    color: white;
    text-decoration: none;
    font-weight: bold;

    &:hover {
      text-decoration: underline;
    }
  }
`;

const S_HamburgerButton = styled.button`
  background: none;
  border: none;
  color: white;
  font-size: 28px;
  cursor: pointer;
  margin-right: 15px;

  ${media.desktop`
    display: none;
  `}
`;

const S_LoggedInInfo = styled.div`
  display: flex;
  align-items: center;
  color: white;
  margin-right: 15px;
  font-weight: bold;
`;

const Header = ({ onToggleSidebar }) => {
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [isSignupModalOpen, setIsSignupModalOpen] = useState(false);

  const isLoggedIn = useSelector((state) => state.login.isLoggedIn);
  const username = useSelector((state) => state.login.username);

  const dispatch = useDispatch();

  const handleOpenLoginModal = () => setIsLoginModalOpen(true);
  const handleCloseLoginModal = () => setIsLoginModalOpen(false);
  const handleOpenSignupModal = () => setIsSignupModalOpen(true);
  const handleCloseSignupModal = () => setIsSignupModalOpen(false);

  const handleLogout = () => {
    if (window.confirm('로그아웃 하시겠습니까?')) {
      dispatch(logout());
      // 로그아웃 후 필요한 추가 동작
    }
  };

  return (
    <S_Header>
      <S_HeaderInner>
        <S_SiteTitleContainer> {/* ✅ S_HamburgerButton과 S_SiteTitle을 함께 묶습니다. */}
          {/* ✅ 햄버거 메뉴 버튼: 모바일/태블릿에서만 보이고 클릭 시 사이드바 토글 */}
          <S_HamburgerButton onClick={onToggleSidebar}>☰</S_HamburgerButton>
          <S_SiteTitle>
            <Link to="/">KAMCO 입찰 정보</Link>
          </S_SiteTitle>
        </S_SiteTitleContainer>
        {isLoggedIn ? (
          <S_MainNav>
            {/* ✅ 사용자 이름 메시지와 ul 메뉴를 flex로 배치 */}
            <ul>
              <li><S_LoggedInInfo>{username}님</S_LoggedInInfo></li> {/* ✅ 별도 스타일 컴포넌트 사용 */}
              <li><Link to="/#" onClick={handleLogout}>로그아웃</Link></li>
              <li><Link to="/mypage">마이페이지</Link></li>
            </ul>
          </S_MainNav>
        ) : (
          <S_MainNav>
            <ul>
              <li><Link to="#" onClick={handleOpenLoginModal}>로그인</Link></li>
              <li><Link to="#" onClick={handleOpenSignupModal}>회원가입</Link></li>
            </ul>
          </S_MainNav>
        )}
      </S_HeaderInner>
      {isLoginModalOpen && (
        <LoginModal
          isOpen={isLoginModalOpen}
          onClose={handleCloseLoginModal}
        />
      )}

      {isSignupModalOpen && (
        <SignupModal
          isOpen={isSignupModalOpen}
          onClose={handleCloseSignupModal}
          onSignupSuccess={() => {
            handleCloseLoginModal();
            handleOpenLoginModal(); // 회원가입 성공 후 로그인 모달을 자동으로 띄우기
            console.log('회원가입 성공 처리');
          }}
        />
      )}
    </S_Header>


  );
};

export default Header;