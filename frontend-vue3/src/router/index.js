import { createRouter, createWebHistory } from "vue-router";
import AuthPage from "../views/AuthPage.vue";
import MenuPage from "../views/MenuPage.vue";
import OrdersPage from "../views/OrdersPage.vue";
import ProfilePage from "../views/ProfilePage.vue";
import AgentPage from "../views/AgentPage.vue";

const routes = [
  { path: "/", redirect: "/auth" },
  { path: "/auth", component: AuthPage },
  { path: "/menu", component: MenuPage, meta: { requiresAuth: true } },
  { path: "/agent", component: AgentPage, meta: { requiresAuth: true } },
  { path: "/orders", component: OrdersPage, meta: { requiresAuth: true } },
  { path: "/profile", component: ProfilePage, meta: { requiresAuth: true } }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const token = localStorage.getItem("token");
  if (to.meta.requiresAuth && !token) {
    return "/auth";
  }
  if (to.path === "/auth" && token) {
    return "/menu";
  }
  return true;
});

export default router;
