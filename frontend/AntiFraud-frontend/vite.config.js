import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd())
  
  return {
    plugins: [
      vue(),
      vueDevTools(),
    ],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
    server: {
      port: 5173,
      proxy: {
        '/user': {
          target: env.VITE_BASE_URL || 'http://localhost:8080',
          changeOrigin: true
        },
        '/admin': {
          target: env.VITE_BASE_URL || 'http://localhost:8080',
          changeOrigin: true
        },
        '/notice': {
          target: env.VITE_BASE_URL || 'http://localhost:8080',
          changeOrigin: true
        },
        '/auth': {
          target: env.VITE_BASE_URL || 'http://localhost:8080',
          changeOrigin: true
        }
      }
    }
  }
})
