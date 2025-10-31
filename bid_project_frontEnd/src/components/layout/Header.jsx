import React from 'react';
import styled from 'styled-components';
import { Link } from 'react-router-dom';

// Header styled components
const S_Header = styled.header`
  background-color: #283747; /* 다크 블루 계열 */
  color: white;
  padding: 15px 20px;
  border-bottom: 1px solid #1a242f;
`;

const S_HeaderInner = styled.div`
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
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
  }

  li {
    margin-left: 20px;
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

const Header = () => {
    return (
        <S_Header>
            <S_HeaderInner>
                <S_SiteTitle>
                    <Link to="/">KAMCO 입찰 정보</Link>
                </S_SiteTitle>
                <S_MainNav>
                    <ul>
                        <li><Link to="/login">로그인</Link></li>
                        <li><Link to="/signup">회원가입</Link></li>
                        <li><Link to="/mypage">마이페이지</Link></li>
                        {/* 필요에 따라 추가 메뉴 */}
                    </ul>
                </S_MainNav>
            </S_HeaderInner>
        </S_Header>
    );
};

export default Header;