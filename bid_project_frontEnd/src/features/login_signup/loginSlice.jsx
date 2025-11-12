import { createSlice } from '@reduxjs/toolkit';
import React from 'react';

const getInitialState = () => {
  const accessToken = localStorage.getItem('accessToken');
  const username = localStorage.getItem('username'); // 로그인 시 백엔드에서 사용자 이름도 같이 전달한다고 가정
  const userId = localStorage.getItem('userId');   // 로그인 시 백엔드에서 사용자 ID도 같이 전달한다고 가정
  const email = localStorage.getItem('email');

  return {
    isLoggedIn: !!accessToken, // accessToken이 있으면 로그인된 상태로 간주
    accessToken: accessToken || null,
    username: username || null,
    userId: userId || null,
    email: email || null,
    status: 'idle', // 'idle' | 'loading' | 'succeeded' | 'failed'
    error: null,
  };
};

const loginSlice = createSlice({
  name: 'login',
  initialState: getInitialState(), // 초기 상태 로드
  reducers: {
    // 로그인 성공 시 상태 변경
    loginSuccess: (state, action) => {
      state.isLoggedIn = true;
      state.accessToken = action.payload.accessToken;
      state.username = action.payload.username; // 사용자 이름 저장
      state.userId = action.payload.userId;   // 사용자 ID 저장
      state.email = action.payload.email; // ✅ email 정보 저장
      state.status = 'succeeded';
      state.error = null;
      // localStorage에 토큰 및 사용자 정보 저장 (새로고침 시 유지)
      localStorage.setItem('accessToken', action.payload.accessToken);
      localStorage.setItem('username', action.payload.username);
      localStorage.setItem('userId', action.payload.userId);
      localStorage.setItem('email', action.payload.email);
    },
    // 로그아웃 시 상태 변경
    logout: (state) => {
      state.isLoggedIn = false;
      state.accessToken = null;
      state.username = null;
      state.userId = null;
      state.email = null; 
      state.status = 'idle';
      state.error = null;

      // localStorage에서 토큰 및 사용자 정보 제거
      localStorage.removeItem('accessToken');
      localStorage.removeItem('username');
      localStorage.removeItem('userId');
      localStorage.removeItem('email');
    },
    // 로그인 실패 시 상태 변경 (옵션)
    loginFailed: (state, action) => {
      state.isLoggedIn = false;
      state.accessToken = null;
      state.username = null;
      state.userId = null;
      state.email = null; 
      state.status = 'failed';
      state.error = action.payload;
      localStorage.removeItem('accessToken');
      localStorage.removeItem('username');
      localStorage.removeItem('userId');
      localStorage.removeItem('email'); 
    },
    // 로그인 시도 중 상태 (옵션)
    loginLoading: (state) => {
      state.status = 'loading';
      state.error = null;
    },
     updateProfileSuccess: (state, action) => {
        state.username = action.payload.username; // 사용자 이름은 그대로 유지될 수도, 변경될 수도 있습니다.
        state.email = action.payload.email;     // ✅ 이메일 정보 업데이트
        state.status = 'succeeded'; // 상태를 성공으로 설정 (필요에 따라)
        state.error = null;
        
        // localStorage도 업데이트
        localStorage.setItem('username', action.payload.username); // username이 바뀔 수도 있다면
        localStorage.setItem('email', action.payload.email);
    }
  },
  // 비동기 액션 처리 (RTK Query 또는 createAsyncThunk 사용 시)
  // extraReducers: (builder) => { ... }
});

export const { loginSuccess, logout, loginFailed, loginLoading, updateProfileSuccess } = loginSlice.actions;

export default loginSlice.reducer;