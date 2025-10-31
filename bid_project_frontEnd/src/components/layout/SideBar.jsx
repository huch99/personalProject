import React from 'react';
import { Link } from 'react-router-dom';
import styled from 'styled-components';

// SideBar styled components
const S_Aside = styled.aside`
  width: 220px;
  background-color: #e9ecef;
  padding: 20px;
  border-right: 1px solid #dee2e6;
  flex-shrink: 0;
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

const SideBar = () => {
    return (
        <S_Aside>
            <S_SideBarNav>
                <ul>
                    <li><Link to="/">🏡 홈</Link></li>
                    <li><Link to="/tenders">📝 입찰 목록</Link></li>
                    <li><Link to="/advanced-search">🔎 고급 검색</Link></li>
                    <li><Link to="/mypage">👤 마이페이지</Link></li>
                    <li><Link to="/faq">❓ FAQ</Link></li>
                </ul>
            </S_SideBarNav>
        </S_Aside>
    );
};

export default SideBar;