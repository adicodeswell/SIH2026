import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'


import { fileURLToPath, URL } from 'node:url'
// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    proxy: {
      '/api/v1/applications': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
      },
      '/api/v1/services': {
        target: 'http://127.0.0.1:8081',
        changeOrigin: true,
      },
      '/api/v1/interop': {
        target: 'http://127.0.0.1:8082',
        changeOrigin: true,
      },
      '/api/v1/workflow': {
        target: 'http://127.0.0.1:8083',
        changeOrigin: true,
      },
      '/api/v1/officer': {
        target: 'http://127.0.0.1:8083',
        changeOrigin: true,
      },
      '/api/v1/consents': {
        target: 'http://127.0.0.1:8083',
        changeOrigin: true,
      }
    }
  }
})
