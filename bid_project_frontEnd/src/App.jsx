import { Provider } from "react-redux"
import { BrowserRouter, Route, Routes } from "react-router-dom"
import { store } from "./app/store"
import HomePage from "./pages/HomePage"
import Layout from './components/layout/Layout'
import FaqBoard from "./pages/FaqBoard"
import FaqDetail from "./pages/FaqDetail"
import FaqWrite from "./pages/FaqWrite"

function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout><HomePage /></Layout>} />
          {/* <Route path="/tenders" element={<Layout><BidListPage /></Layout>} /> */}
          {/* <Route path="/tenders/:id" element={<Layout><BidDetailPage /></Layout>} />  */}
          {/* <Route path="/mypage" element={<Layout><MyPage /></Layout>} /> */}
          {/* <Route path="/advanced-search" element={<Layout><AdvancedSearchPage /></Layout>} /> */}
          {/* <Route path="/payment" element={<Layout><PaymentPage /></Layout>} /> */}
          <Route path="/faq" element={<Layout><FaqBoard /></Layout>} />
          <Route path="/faq/:faqId" element={<Layout><FaqDetail /></Layout>} />
          <Route path="/faq/write" element={<Layout><FaqWrite /></Layout>} />

          <Route path="*" element={<Layout><div><p>죄송합니다. 요청하신 페이지를 찾을 수 없습니다. (404 Not Found)</p></div></Layout>} />
        </Routes>
      </BrowserRouter>
    </Provider>
  )
}

export default App
