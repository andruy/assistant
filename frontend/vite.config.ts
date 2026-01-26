import { defineConfig } from 'vite'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig(() => {
  const outDirArg = process.argv.find(arg => arg.startsWith('--outDir='))
  const outDir = outDirArg == '1' ? path.resolve(__dirname, '../backend/src/main/resources/static') : 'dist'

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
      outDir,
    },
    server: {
      proxy: {
        '/api': 'http://localhost:8080',
        '/login': 'http://localhost:8080',
        '/logout': 'http://localhost:8080',
      },
    },
  }
})
