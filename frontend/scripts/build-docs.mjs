import { execSync } from 'child_process'
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const rootDir = path.resolve(__dirname, '../..')
const frontendDir = path.resolve(__dirname, '..')
const distDir = path.resolve(frontendDir, 'dist')
const docsDir = path.resolve(rootDir, 'docs')

console.log('>>> [1/4] Building Vite project in mock mode...')
execSync('npx vite build --mode mock', { cwd: frontendDir, stdio: 'inherit' })

console.log('>>> [2/4] Cleaning and updating docs directory...')
fs.rmSync(docsDir, { recursive: true, force: true })
fs.mkdirSync(docsDir, { recursive: true })

fs.cpSync(distDir, docsDir, { recursive: true })

console.log('>>> [3/4] Creating 404.html for SPA routing...')
const indexHtmlPath = path.join(docsDir, 'index.html')
const notFoundHtmlPath = path.join(docsDir, '404.html')
if (fs.existsSync(indexHtmlPath)) {
  fs.copyFileSync(indexHtmlPath, notFoundHtmlPath)
}

console.log('>>> [4/4] Creating .nojekyll for GitHub Pages...')
fs.writeFileSync(path.join(docsDir, '.nojekyll'), '')

console.log('>>> ✅ Build for GitHub Pages (/docs) completed successfully!')
