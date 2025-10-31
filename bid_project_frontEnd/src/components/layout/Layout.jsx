import React from 'react';
import styled from 'styled-components';
import Header from './Header';
import SideBar from './SideBar';
import Footer from './Footer';

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
`;

// Content styled component
const S_Content = styled.main`
  flex: 1; /* SideBar 옆의 남은 공간을 차지 */
  padding: 20px;
  background-color: #f8f9fa; /* 콘텐츠 영역 배경색 */
`;

const Layout = ({ children }) => {
    return (
        <S_MainContainer>
            <Header />
            <S_ContentWrapper>
                <SideBar />
                <S_Content>
                    {children}
                </S_Content>
            </S_ContentWrapper>
            <Footer />
        </S_MainContainer>
    );
};

export default Layout;