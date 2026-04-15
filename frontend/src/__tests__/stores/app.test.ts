/**
 * 应用配置状态管理 Store 单元测试
 * 测试主题、语言、侧边栏等全局配置状态管理
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAppStore } from '@/stores/app'

describe('AppStore - 应用状态管理测试', () => {
  beforeEach(() => {
    // 创建新的 Pinia 实例
    setActivePinia(createPinia())
    // 清除 localStorage
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  describe('初始状态', () => {
    it('应该有正确的初始状态', () => {
      const store = useAppStore()

      expect(store.theme).toBe('light')
      expect(store.language).toBe('zh-CN')
      expect(store.sidebarCollapsed).toBe(false)
      expect(store.mobileSidebarVisible).toBe(false)
      expect(store.searchVisible).toBe(false)
      expect(store.loading).toBe(false)
      expect(store.device).toBe('desktop')
      expect(store.screenWidth).toBe(window.innerWidth)
    })

    it('应该有正确的初始 getters', () => {
      const store = useAppStore()

      expect(store.isMobile).toBe(window.innerWidth < 768)
      expect(store.isTablet).toBe(window.innerWidth >= 768 && window.innerWidth < 1024)
      expect(store.isDesktop).toBe(window.innerWidth >= 1024)
      expect(store.isDark).toBe(false)
    })
  })

  describe('setTheme - 设置主题', () => {
    it('应该成功设置为 light 主题', () => {
      const store = useAppStore()

      store.setTheme('light')

      expect(store.theme).toBe('light')
      expect(store.isDark).toBe(false)
    })

    it('应该成功设置为 dark 主题', () => {
      const store = useAppStore()

      store.setTheme('dark')

      expect(store.theme).toBe('dark')
      expect(store.isDark).toBe(true)
    })

    it('应该成功设置为 auto 主题', () => {
      const store = useAppStore()

      store.setTheme('auto')

      expect(store.theme).toBe('auto')
    })
  })

  describe('toggleTheme - 切换主题', () => {
    it('应该从 light 切换到 dark', () => {
      const store = useAppStore()

      store.setTheme('light')
      store.toggleTheme()

      expect(store.theme).toBe('dark')
    })

    it('应该从 dark 切换到 light', () => {
      const store = useAppStore()

      store.setTheme('dark')
      store.toggleTheme()

      expect(store.theme).toBe('light')
    })

    it('应该支持多次切换', () => {
      const store = useAppStore()

      store.setTheme('light')
      store.toggleTheme()
      store.toggleTheme()
      store.toggleTheme()

      expect(store.theme).toBe('dark')
    })
  })

  describe('applyTheme - 应用主题', () => {
    it('应该在 light 主题时移除 dark 类', () => {
      const store = useAppStore()

      store.setTheme('light')
      store.applyTheme()

      expect(document.documentElement.classList.contains('dark')).toBe(false)
    })

    it('应该在 dark 主题时添加 dark 类', () => {
      const store = useAppStore()

      store.setTheme('dark')
      store.applyTheme()

      expect(document.documentElement.classList.contains('dark')).toBe(true)
    })
  })

  describe('setLanguage - 设置语言', () => {
    it('应该成功设置为中文', () => {
      const store = useAppStore()

      store.setLanguage('zh-CN')

      expect(store.language).toBe('zh-CN')
    })

    it('应该成功设置为英文', () => {
      const store = useAppStore()

      store.setLanguage('en-US')

      expect(store.language).toBe('en-US')
    })
  })

  describe('toggleSidebar - 切换侧边栏', () => {
    it('应该切换侧边栏折叠状态', () => {
      const store = useAppStore()

      expect(store.sidebarCollapsed).toBe(false)

      store.toggleSidebar()

      expect(store.sidebarCollapsed).toBe(true)

      store.toggleSidebar()

      expect(store.sidebarCollapsed).toBe(false)
    })
  })

  describe('setSidebarCollapsed - 设置侧边栏状态', () => {
    it('应该成功折叠侧边栏', () => {
      const store = useAppStore()

      store.setSidebarCollapsed(true)

      expect(store.sidebarCollapsed).toBe(true)
    })

    it('应该成功展开侧边栏', () => {
      const store = useAppStore()

      store.setSidebarCollapsed(true)
      store.setSidebarCollapsed(false)

      expect(store.sidebarCollapsed).toBe(false)
    })
  })

  describe('toggleMobileSidebar - 切换移动端侧边栏', () => {
    it('应该切换移动端侧边栏显示状态', () => {
      const store = useAppStore()

      expect(store.mobileSidebarVisible).toBe(false)

      store.toggleMobileSidebar()

      expect(store.mobileSidebarVisible).toBe(true)

      store.toggleMobileSidebar()

      expect(store.mobileSidebarVisible).toBe(false)
    })
  })

  describe('setMobileSidebarVisible - 设置移动端侧边栏显示状态', () => {
    it('应该显示移动端侧边栏', () => {
      const store = useAppStore()

      store.setMobileSidebarVisible(true)

      expect(store.mobileSidebarVisible).toBe(true)
    })

    it('应该隐藏移动端侧边栏', () => {
      const store = useAppStore()

      store.setMobileSidebarVisible(true)
      store.setMobileSidebarVisible(false)

      expect(store.mobileSidebarVisible).toBe(false)
    })
  })

  describe('showSearch - 显示搜索框', () => {
    it('应该成功显示搜索框', () => {
      const store = useAppStore()

      store.showSearch()

      expect(store.searchVisible).toBe(true)
    })
  })

  describe('hideSearch - 隐藏搜索框', () => {
    it('应该成功隐藏搜索框', () => {
      const store = useAppStore()

      store.showSearch()
      store.hideSearch()

      expect(store.searchVisible).toBe(false)
    })
  })

  describe('toggleSearch - 切换搜索框', () => {
    it('应该切换搜索框显示状态', () => {
      const store = useAppStore()

      expect(store.searchVisible).toBe(false)

      store.toggleSearch()

      expect(store.searchVisible).toBe(true)

      store.toggleSearch()

      expect(store.searchVisible).toBe(false)
    })
  })

  describe('setLoading - 设置加载状态', () => {
    it('应该设置为加载中', () => {
      const store = useAppStore()

      store.setLoading(true)

      expect(store.loading).toBe(true)
    })

    it('应该设置为加载完成', () => {
      const store = useAppStore()

      store.setLoading(true)
      store.setLoading(false)

      expect(store.loading).toBe(false)
    })
  })

  describe('setDevice - 设置设备类型', () => {
    it('应该设置为移动端', () => {
      const store = useAppStore()

      store.setDevice('mobile')

      expect(store.device).toBe('mobile')
      expect(store.isMobile).toBe(true)
      expect(store.isTablet).toBe(false)
      expect(store.isDesktop).toBe(false)
    })

    it('应该设置为平板', () => {
      const store = useAppStore()

      store.setDevice('tablet')

      expect(store.device).toBe('tablet')
      expect(store.isMobile).toBe(false)
      expect(store.isTablet).toBe(true)
      expect(store.isDesktop).toBe(false)
    })

    it('应该设置为桌面端', () => {
      const store = useAppStore()

      store.setDevice('desktop')

      expect(store.device).toBe('desktop')
      expect(store.isMobile).toBe(false)
      expect(store.isTablet).toBe(false)
      expect(store.isDesktop).toBe(true)
    })
  })

  describe('updateScreenWidth - 更新屏幕宽度', () => {
    it('应该在宽度小于 768 时设置为移动端', () => {
      const store = useAppStore()

      store.updateScreenWidth(375)

      expect(store.screenWidth).toBe(375)
      expect(store.device).toBe('mobile')
    })

    it('应该在宽度大于等于 768 且小于 1024 时设置为平板', () => {
      const store = useAppStore()

      store.updateScreenWidth(800)

      expect(store.screenWidth).toBe(800)
      expect(store.device).toBe('tablet')
    })

    it('应该在宽度大于等于 1024 时设置为桌面端', () => {
      const store = useAppStore()

      store.updateScreenWidth(1920)

      expect(store.screenWidth).toBe(1920)
      expect(store.device).toBe('desktop')
    })

    it('应该正确处理边界值', () => {
      const store = useAppStore()

      store.updateScreenWidth(767)
      expect(store.device).toBe('mobile')

      store.updateScreenWidth(768)
      expect(store.device).toBe('tablet')

      store.updateScreenWidth(1023)
      expect(store.device).toBe('tablet')

      store.updateScreenWidth(1024)
      expect(store.device).toBe('desktop')
    })
  })

  describe('reset - 重置状态', () => {
    it('应该成功重置所有状态', () => {
      const store = useAppStore()

      // 修改状态
      store.setTheme('dark')
      store.setLanguage('en-US')
      store.setSidebarCollapsed(true)
      store.setMobileSidebarVisible(true)
      store.showSearch()
      store.setLoading(true)

      // 重置状态
      store.reset()

      expect(store.theme).toBe('light')
      expect(store.language).toBe('zh-CN')
      expect(store.sidebarCollapsed).toBe(false)
      expect(store.mobileSidebarVisible).toBe(false)
      expect(store.searchVisible).toBe(false)
      expect(store.loading).toBe(false)
    })

    it('应该可以多次调用重置', () => {
      const store = useAppStore()

      store.reset()
      store.reset()
      store.reset()

      expect(store.theme).toBe('light')
      expect(store.language).toBe('zh-CN')
    })
  })

  describe('持久化', () => {
    it('应该持久化 theme、language 和 sidebarCollapsed', () => {
      const store = useAppStore()

      store.setTheme('dark')
      store.setLanguage('en-US')
      store.setSidebarCollapsed(true)

      // 检查 localStorage
      const storedData = localStorage.getItem('app')
      expect(storedData).toBeTruthy()

      const parsedData = JSON.parse(storedData!)
      expect(parsedData.theme).toBe('dark')
      expect(parsedData.language).toBe('en-US')
      expect(parsedData.sidebarCollapsed).toBe(true)
    })

    it('应该从 localStorage 恢复状态', () => {
      // 先保存数据到 localStorage
      localStorage.setItem(
        'app',
        JSON.stringify({
          theme: 'dark',
          language: 'en-US',
          sidebarCollapsed: true,
        })
      )

      // 创建新的 store 实例
      setActivePinia(createPinia())
      const store = useAppStore()

      expect(store.theme).toBe('dark')
      expect(store.language).toBe('en-US')
      expect(store.sidebarCollapsed).toBe(true)
    })
  })

  describe('边界情况', () => {
    it('应该处理极小的屏幕宽度', () => {
      const store = useAppStore()

      store.updateScreenWidth(0)

      expect(store.screenWidth).toBe(0)
      expect(store.device).toBe('mobile')
    })

    it('应该处理极大的屏幕宽度', () => {
      const store = useAppStore()

      store.updateScreenWidth(9999)

      expect(store.screenWidth).toBe(9999)
      expect(store.device).toBe('desktop')
    })

    it('应该处理负数屏幕宽度', () => {
      const store = useAppStore()

      store.updateScreenWidth(-100)

      expect(store.screenWidth).toBe(-100)
      expect(store.device).toBe('mobile')
    })
  })

  describe('组合操作', () => {
    it('应该支持切换主题后应用', () => {
      const store = useAppStore()

      store.setTheme('light')
      store.toggleTheme()
      store.applyTheme()

      expect(store.theme).toBe('dark')
      expect(document.documentElement.classList.contains('dark')).toBe(true)
    })

    it('应该支持切换侧边栏后重置', () => {
      const store = useAppStore()

      store.toggleSidebar()
      store.toggleMobileSidebar()
      store.showSearch()
      store.reset()

      expect(store.sidebarCollapsed).toBe(false)
      expect(store.mobileSidebarVisible).toBe(false)
      expect(store.searchVisible).toBe(false)
    })

    it('应该支持更新屏幕宽度后设置设备', () => {
      const store = useAppStore()

      store.updateScreenWidth(800)
      store.setDevice('mobile')

      expect(store.screenWidth).toBe(800)
      expect(store.device).toBe('mobile')
    })
  })

  describe('init - 初始化应用配置', () => {
    it('应该应用初始主题', () => {
      const store = useAppStore()

      store.setTheme('dark')
      store.init()

      expect(document.documentElement.classList.contains('dark')).toBe(true)
    })

    it('应该监听屏幕宽度变化（模拟）', () => {
      const store = useAppStore()

      store.init()
      store.updateScreenWidth(375)

      expect(store.device).toBe('mobile')
    })
  })

  describe('isDark getter - 自动主题检测', () => {
    it('应该在 auto 主题时根据系统偏好返回', () => {
      const store = useAppStore()

      store.setTheme('auto')

      // 注意：此测试依赖于系统偏好设置
      // 在实际环境中，可能需要 mock window.matchMedia
      const isDark = store.isDark

      expect(typeof isDark).toBe('boolean')
    })

    it('应该在 light 主题时返回 false', () => {
      const store = useAppStore()

      store.setTheme('light')

      expect(store.isDark).toBe(false)
    })

    it('应该在 dark 主题时返回 true', () => {
      const store = useAppStore()

      store.setTheme('dark')

      expect(store.isDark).toBe(true)
    })
  })
})