import React from 'react';
import styled from 'styled-components';

// Footer styled components
const S_Footer = styled.footer`
  background-color: #212529;
  color: #adb5bd;
  padding: 20px;
  text-align: center;
  font-size: 14px;
`;

const Footer = () => {
    return (
        <S_Footer>
            <p>&copy; 2025 KAMCO 입찰 정보 사이트. All rights reserved.</p>
        </S_Footer>
    );
};

export default Footer;