/**
 * 应用配置状态管理 Store
 * 管理应用全局配置，如主题、语言、侧边栏状态等
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export type Theme = 'light' | 'dark' | 'auto'
export type Language = 'zh-CN' | 'en-US'

export const useAppStore = defineStore(
  'app',
  () => {
    // ==========================================
    // State
    // ==========================================

    /**
     * 主题模式
     */
    const theme = ref<Theme>('light')

    /**
     * 语言
     */
    const language = ref<Language>('zh-CN')

    /**
     * 侧边栏是否折叠
     */
    const sidebarCollapsed = ref<boolean>(false)

    /**
     * 移动端侧边栏是否显示
     */
    const mobileSidebarVisible = ref<boolean>(false)

    /**
     * 是否显示搜索框
     */
    const searchVisible = ref<boolean>(false)

    /**
     * 页面加载状态
     */
    const loading = ref<boolean>(false)

    /**
     * 设备类型
     */
    const device = ref<'mobile' | 'tablet' | 'desktop'>('desktop')

    /**
     * 屏幕宽度
     */
    const screenWidth = ref<number>(window.innerWidth)

    // ==========================================
    // Getters
    // ==========================================

    /**
     * 是否为移动端
     */
    const isMobile = computed(() => device.value === 'mobile')

    /**
     * 是否为平板
     */
    const isTablet = computed(() => device.value === 'tablet')

    /**
     * 是否为桌面端
     */
    const isDesktop = computed(() => device.value === 'desktop')

    /**
     * 是否为暗黑模式
     */
    const isDark = computed(() => {
      if (theme.value === 'auto') {
        return window.matchMedia('(prefers-color-scheme: dark)').matches
      }
      return theme.value === 'dark'
    })

    // ==========================================
    // Actions
    // ==========================================

    /**
     * 设置主题
     */
    const setTheme = (newTheme: Theme) => {
      theme.value = newTheme
      applyTheme()
    }

    /**
     * 切换主题
     */
    const toggleTheme = () => {
      theme.value = theme.value === 'light' ? 'dark' : 'light'
      applyTheme()
    }

    /**
     * 应用主题
     */
    const applyTheme = () => {
      const htmlElement = document.documentElement
      if (isDark.value) {
        htmlElement.classList.add('dark')
      } else {
        htmlElement.classList.remove('dark')
      }
    }

    /**
     * 设置语言
     */
    const setLanguage = (newLanguage: Language) => {
      language.value = newLanguage
    }

    /**
     * 切换侧边栏
     */
    const toggleSidebar = () => {
      sidebarCollapsed.value = !sidebarCollapsed.value
    }

    /**
     * 设置侧边栏状态
     */
    const setSidebarCollapsed = (collapsed: boolean) => {
      sidebarCollapsed.value = collapsed
    }

    /**
     * 切换移动端侧边栏
     */
    const toggleMobileSidebar = () => {
      mobileSidebarVisible.value = !mobileSidebarVisible.value
    }

    /**
     * 设置移动端侧边栏显示状态
     */
    const setMobileSidebarVisible = (visible: boolean) => {
      mobileSidebarVisible.value = visible
    }

    /**
     * 显示搜索框
     */
    const showSearch = () => {
      searchVisible.value = true
    }

    /**
     * 隐藏搜索框
     */
    const hideSearch = () => {
      searchVisible.value = false
    }

    /**
     * 切换搜索框
     */
    const toggleSearch = () => {
      searchVisible.value = !searchVisible.value
    }

    /**
     * 设置加载状态
     */
    const setLoading = (isLoading: boolean) => {
      loading.value = isLoading
    }

    /**
     * 设置设备类型
     */
    const setDevice = (newDevice: 'mobile' | 'tablet' | 'desktop') => {
      device.value = newDevice
    }

    /**
     * 更新屏幕宽度
     */
    const updateScreenWidth = (width: number) => {
      screenWidth.value = width
      // 根据宽度自动设置设备类型
      if (width < 768) {
        device.value = 'mobile'
      } else if (width < 1024) {
        device.value = 'tablet'
      } else {
        device.value = 'desktop'
      }
    }

    /**
     * 初始化应用配置
     */
    const init = () => {
      // 应用主题
      applyTheme()

      // 监听屏幕宽度变化
      window.addEventListener('resize', () => {
        updateScreenWidth(window.innerWidth)
      })

      // 监听系统主题变化
      if (theme.value === 'auto') {
        window
          .matchMedia('(prefers-color-scheme: dark)')
          .addEventListener('change', applyTheme)
      }

      // 初始化屏幕宽度
      updateScreenWidth(window.innerWidth)
    }

    /**
     * 重置状态
     */
    const reset = () => {
      theme.value = 'light'
      language.value = 'zh-CN'
      sidebarCollapsed.value = false
      mobileSidebarVisible.value = false
      searchVisible.value = false
      loading.value = false
    }

    return {
      // State
      theme,
      language,
      sidebarCollapsed,
      mobileSidebarVisible,
      searchVisible,
      loading,
      device,
      screenWidth,

      // Getters
      isMobile,
      isTablet,
      isDesktop,
      isDark,

      // Actions
      setTheme,
      toggleTheme,
      applyTheme,
      setLanguage,
      toggleSidebar,
      setSidebarCollapsed,
      toggleMobileSidebar,
      setMobileSidebarVisible,
      showSearch,
      hideSearch,
      toggleSearch,
      setLoading,
      setDevice,
      updateScreenWidth,
      init,
      reset,
    }
  },
  {
    // 持久化配置
    persist: {
      key: 'app',
      storage: localStorage,
      paths: ['theme', 'language', 'sidebarCollapsed'],
    },
  }
)
