import React, { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.jsx'
import { Provider } from 'react-redux'
import { store } from './app/store';

createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Provider store={store}> {/* Provider로 App 컴포넌트를 감쌉니다. */}
      <App />
    </Provider>
  </React.StrictMode>
)
