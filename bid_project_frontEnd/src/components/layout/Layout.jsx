import React, { useState } from 'react';
import styled from 'styled-components';
import Header from './Header';
import SideBar from './SideBar';
import Footer from './Footer';
import media from '../../styles/media';

// MainContainer styled component
const S_MainContainer = styled.div`
  display: flex;
  flex-direction: column;
  min-height: 100vh;
`;

// ContentWrapper styled component
const S_ContentWrapper = styled.div`
  display: flex;
  flex: 1; /* Header와 Footer 사이의 남은 공간을 차지 */

  ${media.desktop`
    // Layout이 Header를 포함하고 SideBar는 fixed였다가 desktop에서 relative로 바뀌므로
    // S_ContentWrapper의 마진은 필요 없거나, S_Content가 마진을 가집니다.
    // 여기서는 S_Content에 margin-left를 주어 Desktop에서 Sidebar가 차지하는 공간을 비워주겠습니다.
  `}
`;

// Content styled component
const S_Content = styled.main`
  flex: 1; /* SideBar 옆의 남은 공간을 차지 */
  padding: 20px;
  background-color: #f8f9fa; /* 콘텐츠 영역 배경색 */

  // --- 반응형: 모바일에서는 패딩 조정 ---
  ${media.tablet`
    padding: 15px; // 태블릿에서 콘텐츠 패딩 약간 줄임
  `}
  ${media.mobileL`
    padding: 10px; // 모바일에서 콘텐츠 패딩 더 줄임
  `}
`;

const Layout = ({ children }) => {

  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const handleToggleSidebar = () => {
    setIsSidebarOpen(!isSidebarOpen);
    // console.log("Sidebar Toggled in Layout. Current state:", !isSidebarOpen); // 디버깅 로그
  };

  const handleCloseSidebar = () => {
    setIsSidebarOpen(false);
    // console.log("Sidebar Closed in Layout"); // 디버깅 로그
  };

  return (
    <S_MainContainer>
      {/* Header 컴포넌트에 onToggleSidebar 함수를 prop으로 전달 */}
      <Header onToggleSidebar={handleToggleSidebar} />

      <S_ContentWrapper>
        {/* SideBar 컴포넌트에 현재 상태와 닫기 함수 전달 */}
        <SideBar isOpen={isSidebarOpen} onClose={handleCloseSidebar} />

        {/* Main Content Area */}
        <S_Content>
          {children} {/* 여기에 라우터로 매핑된 페이지 내용이 들어갑니다. */}
        </S_Content>
      </S_ContentWrapper>

      {/* Footer 컴포넌트 렌더링 */}
      <Footer />
    </S_MainContainer>
  );
};

export default Layout;