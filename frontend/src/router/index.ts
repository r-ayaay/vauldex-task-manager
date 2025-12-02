// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
// import DashboardLayout from '../components/layouts/dashboard-layout.vue'
// import PortfolioView from '../views/PortfolioView.vue'
// import HistoryView from '../views/History-View.vue'
import Login from '../pages/Login.vue'
import Register from '../pages/Register.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { requiresGuest: true },
    },
    {
      path: '/register',
      name: 'register',
      component: () => Register,
      meta: { requiresGuest: true },
    },
    // {
    //   path: '/dashboard',
    //   component: DashboardLayout,
    //   children: [
    //     { path: '', name: 'overview', component: PortfolioView },
    //     { path: 'history', name: 'history', component: HistoryView },
    //   ],

    //   meta: { requiresAuth: true },
    // },

    // default redirect
    { path: '/:pathMatch(.*)*', redirect: '/login' },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()

  // If route requires auth and user is NOT logged in
  if (to.meta.requiresAuth && !auth.token) {
    return '/login'
  }

  // If the route is login/register but user is already logged in
  if (to.meta.requiresGuest && auth.token) {
    return '/dashboard'
  }
})

export default router
