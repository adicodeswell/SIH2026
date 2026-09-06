import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    proxy: {
      '/api/v1/applications': {
        target: 'http://localhost:8081',
        changeOrigin: true,
      },
      '/api/v1/interop': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
      '/api/v1/workflow': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '/api/v1/officer': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      },
      '/api/v1/consents': {
        target: 'http://localhost:8083',
        changeOrigin: true,
      }
    }
  }
})
