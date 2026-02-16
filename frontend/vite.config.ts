import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig(() => {
  return {
    plugins: [
      tailwindcss(),
      react({
        babel: {
          plugins: [['babel-plugin-react-compiler']],
        },
      }),
    ],
    build: {
      outDir: path.resolve(__dirname, '../backend/src/main/resources/static'),
      emptyOutDir: true,
    },
    server: {
      proxy: {
        '/api': 'http://localhost:8080',
        '/login': 'http://localhost:8080',
        '/logout': 'http://localhost:8080',
      },
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      }
    }
  }
})
