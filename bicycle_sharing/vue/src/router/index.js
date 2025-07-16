import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import UserView from '../views/UserView.vue'
import WorkerView from '../views/WorkerView.vue'
import AdminView from '../views/administrator/AdminView.vue'
import profileView from '../views/administrator/profileView.vue'
import dashboardView from "@/views/administrator/dashboardView.vue";
import locationView from "@/views/administrator/locationView.vue";
import tasksView from "@/views/administrator/tasksView.vue";
import helpView from "@/views/administrator/helpView.vue";
import APITestView from '@/views/APITestView.vue'
import APITestManagerStaff from '@/views/APItest_manager_staff.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/register',
      name: 'register',
      component: RegisterView
    },
    {
      path: '/user',
      name: 'user',
      component: UserView
    },
    {
      path: '/admin',
      name: 'admin',
      component: AdminView
    },
    {
      path: '/worker',
      name: 'worker',
      component: WorkerView
    },
    {
      path: '/profile',
      name: 'profile',
      component: profileView
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: dashboardView
    },
    {
      path: '/location',
      name: 'location',
      component: locationView
    },
    {
      path: '/tasks',
      name: 'tasks',
      component: tasksView
    },
    {
      path: '/help',
      name: 'help',
      component: helpView
    },
    {
      path: '/api-test',
      name: 'api-test',
      component: APITestView
    },
    {
      path: '/api-test-manager-staff',
      name: 'api-test-manager-staff',
      component: APITestManagerStaff
    }
  ]
})

export default router