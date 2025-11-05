import React from 'react';
import { Link } from 'react-router-dom';
import styled from 'styled-components';
import media from '../../styles/media';

// SideBar styled components
const S_Aside = styled.aside`
  position: fixed; // 항상 화면에 고정
  top: 0;
  left: 0; // 기본적으로 왼쪽에 붙음
  width: 250px; // 사이드바 너비 (모바일/태블릿에서 열릴 때의 너비)
  height: 100vh; // 화면 전체 높이
  background-color: #e9ecef;
  padding: 20px;
  border-right: 1px solid #dee2e6;
  flex-shrink: 0;
  z-index: 1050; // 모달보다 z-index 낮게 (모달 1050), 헤더보다는 높게
  box-shadow: 2px 0 5px rgba(0,0,0,0.2); // 그림자 효과

  // 사이드바 애니메이션
  transform: translateX(${(props) => (props.$isOpen ? '0' : '-100%')});
  transition: transform 0.3s ease-in-out;

  // --- 반응형: 태블릿 이하에서는 사이드바 숨기거나 다른 형태로 표시 ---
  ${media.tablet`
    border-right: none; // 줄이면 테두리 제거
  `}

   // 데스크탑에서는 항상 보임
  ${media.desktop`
    display: block;
    position: relative; // 데스크탑에서는 일반적인 흐름으로
    transform: translateX(0); // 애니메이션 제거
    width: 220px; // 데스크탑 기본 너비
    box-shadow: none; // 그림자 제거
    border-right: 1px solid #dee2e6;
  `}
`;

const S_SideBarNav = styled.nav`
  ul {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  li {
    margin-bottom: 10px;
  }

  a {
    text-decoration: none;
    color: #343a40;
    font-weight: 500;
    display: block;
    padding: 8px 10px;
    border-radius: 4px;
    transition: background-color 0.2s ease;

    &:hover {
      background-color: #ced4da;
    }
  }
`;

const S_CloseButton = styled.button`
  position: absolute;
  top: 15px;
  right: 15px;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #555;

  ${media.desktop`
    display: none;
  `}
`;

const S_Overlay = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1040;
  display: ${(props) => (props.$isOpen ? 'block' : 'none')};

  ${media.desktop`
    display: none;
  `}
`;



const SideBar = ({ isOpen, onClose }) => {
  return (
    <>
      <S_Overlay $isOpen={isOpen} onClick={onClose} />
      <S_Aside $isOpen={isOpen}>
        <S_CloseButton onClick={onClose}>&times;</S_CloseButton>
        <S_SideBarNav>
          <ul>
            <li><Link to="/" onClick={onClose}>🏡 홈</Link></li> {/* ✅ onClose 추가: 클릭 시 사이드바 닫힘 */}
            <li><Link to="/tenders" onClick={onClose}>📝 입찰 목록</Link></li>
            <li><Link to="/advanced-search" onClick={onClose}>🔎 고급 검색</Link></li>
            <li><Link to="/mypage" onClick={onClose}>👤 마이페이지</Link></li>
            <li><Link to="/faq" onClick={onClose}>❓ FAQ</Link></li>
          </ul>
        </S_SideBarNav>
      </S_Aside>
    </>
  );
};

export default SideBar;