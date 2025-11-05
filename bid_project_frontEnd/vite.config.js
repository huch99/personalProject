import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': { // '/api'로 시작하는 모든 요청을 프록시합니다.
        target: 'http://localhost:8080', // Spring Boot 백엔드 서버 주소
        changeOrigin: true, // 대상 서버의 출처를 변경하여 CORS 문제를 해결
        rewrite: (path) => path.replace(/^\/api/, '') // (선택 사항) '/api'를 요청 경로에서 제거합니다.
                                                        // 예를 들어, /api/tenders -> http://localhost:8080/tenders 로 요청
                                                        // 백엔드 URL 매핑이 '/api/tenders' 라면 이 줄을 제거해야 합니다.
      },
    },
  },
})
