/**
 * Vue Router 路由配置
 * CampusZhihu 前端路由
 */

import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

// 配置 NProgress
NProgress.configure({
  showSpinner: false,
  trickleSpeed: 200,
  minimum: 0.3,
})

/**
 * 路由配置
 */
const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/Layout.vue'),
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: {
          title: '首页',
          requiresAuth: false,
        },
      },
      {
        path: '/questions',
        name: 'Questions',
        component: () => import('@/views/Questions.vue'),
        meta: {
          title: '问题广场',
          requiresAuth: false,
        },
      },
      {
        path: '/question/:id',
        name: 'QuestionDetail',
        component: () => import('@/views/QuestionDetail.vue'),
        meta: {
          title: '问题详情',
          requiresAuth: false,
        },
      },
      {
        path: '/ask',
        name: 'AskQuestion',
        component: () => import('@/views/AskQuestion.vue'),
        meta: {
          title: '提问',
          requiresAuth: true,
        },
      },
      {
        path: '/tags',
        name: 'Tags',
        component: () => import('@/views/Tags.vue'),
        meta: {
          title: '话题标签',
          requiresAuth: false,
        },
      },
      {
        path: '/tag/:id',
        name: 'TagDetail',
        component: () => import('@/views/TagDetail.vue'),
        meta: {
          title: '话题详情',
          requiresAuth: false,
        },
      },
      {
        path: '/user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/Profile.vue'),
        meta: {
          title: '个人中心',
          requiresAuth: true,
        },
      },
      {
        path: '/user/questions',
        name: 'UserQuestions',
        component: () => import('@/views/user/MyQuestions.vue'),
        meta: {
          title: '我的提问',
          requiresAuth: true,
        },
      },
      {
        path: '/user/answers',
        name: 'UserAnswers',
        component: () => import('@/views/user/MyAnswers.vue'),
        meta: {
          title: '我的回答',
          requiresAuth: true,
        },
      },
      {
        path: '/user/collections',
        name: 'UserCollections',
        component: () => import('@/views/user/MyCollections.vue'),
        meta: {
          title: '我的收藏',
          requiresAuth: true,
        },
      },
      {
        path: '/user/points',
        name: 'UserPoints',
        component: () => import('@/views/user/MyPoints.vue'),
        meta: {
          title: '我的积分',
          requiresAuth: true,
        },
      },
      {
        path: '/user/notices',
        name: 'UserNotices',
        component: () => import('@/views/user/MyNotices.vue'),
        meta: {
          title: '消息通知',
          requiresAuth: true,
        },
      },
    ],
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false,
    },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: {
      title: '注册',
      requiresAuth: false,
    },
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      requiresAuth: false,
    },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
  },
]

/**
 * 创建路由实例
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

/**
 * 全局前置守卫
 */
router.beforeEach((to, from, next) => {
  // 开始进度条
  NProgress.start()

  // 设置页面标题
  const title = to.meta.title as string
  if (title) {
    document.title = `${title} - CampusZhihu`
  } else {
    document.title = 'CampusZhihu - 校园知乎'
  }

  // 获取用户状态
  const userStore = useUserStore()
  const isLoggedIn = userStore.hasToken

  // 判断是否需要登录
  if (to.meta.requiresAuth) {
    if (isLoggedIn) {
      next()
    } else {
      // 未登录，跳转到登录页
      next({
        path: '/login',
        query: { redirect: to.fullPath },
      })
    }
  } else {
    // 如果已登录且访问登录/注册页，跳转到首页
    if ((to.path === '/login' || to.path === '/register') && isLoggedIn) {
      next({ path: '/' })
    } else {
      next()
    }
  }
})

/**
 * 全局后置钩子
 */
router.afterEach(() => {
  // 结束进度条
  NProgress.done()
})

/**
 * 路由错误处理
 */
router.onError((error) => {
  console.error('路由错误：', error)
  NProgress.done()
})

export default router
