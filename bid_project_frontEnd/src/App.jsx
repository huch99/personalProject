import { Provider } from "react-redux"
import { BrowserRouter, Route, Routes } from "react-router-dom"
import { store } from "./app/store"
import HomePage from "./pages/HomePage"
import Layout from './components/layout/Layout'
import FaqBoard from "./pages/faqPages/FaqBoard"
import FaqDetail from "./pages/faqPages/FaqDetail"
import FaqWrite from "./pages/faqPages/FaqWrite"
import FaqEdit from "./pages/faqPages/FaqEdit"
import DetailSearchPage from "./pages/DetailSearchPage"
import TenderDetailPage from "./pages/tenderPages/TenderDetailPage"
import MyPage from "./pages/myPages/MyPage"


function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <Routes>
          {/* 홈페이지 루트 */}
          <Route path="/" element={<Layout><HomePage /></Layout>} />

          {/* 마이페이지 루트 */}
          <Route path="/mypage" element={<Layout><MyPage /></Layout>} />

          {/* 상세 검색 페이지 루트 */}
          <Route path="/search" element={<Layout><DetailSearchPage /></Layout>}/>

          {/* 공매물 루트 */}
          <Route path="/tenders/:cltrMnmtNo" element={<Layout> <TenderDetailPage /> </Layout>} />

          {/* 게시판 루트 */}
          <Route path="/faq" element={<Layout><FaqBoard /></Layout>} />
          <Route path="/faq/:faqId" element={<Layout><FaqDetail /></Layout>} />
          <Route path="/faq/write" element={<Layout><FaqWrite /></Layout>} />
          <Route path="/faq/:faqId/edit" element={<Layout><FaqEdit /></Layout>}></Route>

          <Route path="*" element={<Layout><div><p>죄송합니다. 요청하신 페이지를 찾을 수 없습니다. (404 Not Found)</p></div></Layout>} />
        </Routes>
      </BrowserRouter>
    </Provider>
  )
}

export default App
