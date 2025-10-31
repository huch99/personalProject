import { Provider } from "react-redux"
import { BrowserRouter, Route, Routes } from "react-router-dom"
import { store } from "./app/store"
import HomePage from "./pages/HomePage"
import Layout from './components/layout/Layout'

function App() {
  return (
    <Provider store={store}>
      {/* HTML5 History API를 사용하여 UI와 URL을 동기화합니다. */}
      <BrowserRouter>
        {/* URL 경로에 따라 렌더링될 컴포넌트를 정의합니다. */}
        <Routes>
          {/* 
            'Layout' 컴포넌트로 감싸진 Route들은 공통 Header, SideBar, Footer를 가집니다.
            각 페이지 컴포넌트(HomePage, BidListPage 등)는 Layout의 'children' prop으로 전달됩니다.
          */}
          <Route path="/" element={<Layout><HomePage /></Layout>} />
          {/* <Route path="/tenders" element={<Layout><BidListPage /></Layout>} /> */}
          {/* 콜론(:)을 사용하여 동적 파라미터(id)를 정의합니다. */}
          {/* <Route path="/tenders/:id" element={<Layout><BidDetailPage /></Layout>} /> 
          <Route path="/mypage" element={<Layout><MyPage /></Layout>} />
          <Route path="/advanced-search" element={<Layout><AdvancedSearchPage /></Layout>} />
          <Route path="/payment" element={<Layout><PaymentPage /></Layout>} />
          <Route path="/faq" element={<Layout><FaqPage /></Layout>} /> */}

          {/* 
            로그인/회원가입 페이지처럼 특정 페이지는 전체 Layout 없이 
            독립적인 디자인을 가질 수 있도록 Layout으로 감싸지 않았습니다.
          */}
          {/* <Route path="/login" element={<LoginPage />} /> */}
          {/* <Route path="/signup" element={<SignupPage />} /> */}

          {/* 
            위에 정의된 어떤 경로와도 일치하지 않을 때 보여줄 404 (Not Found) 페이지입니다.
            Layout을 적용하여 통일된 디자인을 유지할 수 있습니다.
          */}
          <Route path="*" element={<Layout><div><p>죄송합니다. 요청하신 페이지를 찾을 수 없습니다. (404 Not Found)</p></div></Layout>} />
        </Routes>
      </BrowserRouter>
    </Provider>
  )
}

export default App
