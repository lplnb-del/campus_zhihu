import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    // Element Plus 按需引入
    AutoImport({
      resolvers: [ElementPlusResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
      eslintrc: {
        enabled: true,
      },
    }),
    Components({
      resolvers: [
        ElementPlusResolver({
          // 自动引入修改主题色添加 importStyle: 'sass'
          importStyle: 'sass',
        }),
      ],
      dts: 'src/components.d.ts',
    }),
  ],

  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '~': resolve(__dirname, 'src'),
    },
    extensions: ['.js', '.json', '.ts', '.vue'],
  },

  // CSS 预处理器配置
  css: {
    preprocessorOptions: {
      scss: {
        // Element Plus 主题色自定义
        additionalData: `@use "@/assets/styles/element-vars.scss" as *;`,
      },
    },
  },

  // 服务器配置
  server: {
    port: 3000,
    host: '0.0.0.0',
    open: true,
    cors: true,
    // 代理配置
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '/api'),
      },
    },
  },

  // GitHub Pages 部署路径
  base: '/campus_zhihu/',

  // 构建配置
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    // 消除打包大小超过 500kb 警告
    chunkSizeWarningLimit: 2000,
    rollupOptions: {
      output: {
        // 静态资源分类打包
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]',
        // 代码分割
        manualChunks(id) {
          if (id.includes('node_modules')) {
            // 将 node_modules 中的代码单独打包成一个 JS 文件
            return 'vendor'
          }
        },
      },
    },
    // 压缩配置
    minify: 'terser',
    terserOptions: {
      compress: {
        // 生产环境移除 console
        drop_console: true,
        drop_debugger: true,
      },
    },
  },

  // 优化配置
  optimizeDeps: {
    include: [
      'vue',
      'vue-router',
      'pinia',
      'axios',
      'element-plus/es',
      'element-plus/es/components/message/style/css',
      'element-plus/es/components/message-box/style/css',
      'element-plus/es/components/notification/style/css',
      '@element-plus/icons-vue',
    ],
  },
})
