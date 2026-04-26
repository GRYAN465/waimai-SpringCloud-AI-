<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import api from "../api";

const router = useRouter();
const tab = ref("login");
const loading = ref(false);
const error = ref("");

const loginForm = ref({
  username: "demo",
  password: "123456"
});

const registerForm = ref({
  username: "",
  password: "",
  name: "",
  phone: ""
});

function saveAuth(data) {
  localStorage.setItem("token", data.token);
  localStorage.setItem("userId", data.userId);
  localStorage.setItem("username", data.username);
  localStorage.setItem("name", data.name);
}

async function handleLogin() {
  loading.value = true;
  error.value = "";
  try {
    const resp = await api.post("/auth/login", loginForm.value);
    if (resp.data.code !== 0) {
      error.value = resp.data.message || "登录失败";
      return;
    }
    saveAuth(resp.data.data);
    router.push("/menu");
  } catch (e) {
    error.value = "登录失败，请检查网关和认证服务";
  } finally {
    loading.value = false;
  }
}

async function handleRegister() {
  loading.value = true;
  error.value = "";
  try {
    const resp = await api.post("/auth/register", registerForm.value);
    if (resp.data.code !== 0) {
      error.value = resp.data.message || "注册失败";
      return;
    }
    saveAuth(resp.data.data);
    router.push("/menu");
  } catch (e) {
    error.value = "注册失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-card card">
      <h2>欢迎来到 Cloud Takeout</h2>
      <p>青绿色主题点餐系统，登录后即可开始下单。</p>

      <div class="tab-row">
        <button class="tab-btn" :class="{ active: tab === 'login' }" @click="tab = 'login'">登录</button>
        <button class="tab-btn" :class="{ active: tab === 'register' }" @click="tab = 'register'">注册</button>
      </div>

      <form v-if="tab === 'login'" @submit.prevent="handleLogin" class="form-grid">
        <label>账号</label>
        <input v-model="loginForm.username" placeholder="请输入用户名" />
        <label>密码</label>
        <input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        <button :disabled="loading" class="primary-btn">{{ loading ? "登录中..." : "登录" }}</button>
      </form>

      <form v-else @submit.prevent="handleRegister" class="form-grid">
        <label>用户名</label>
        <input v-model="registerForm.username" placeholder="请输入用户名" />
        <label>密码</label>
        <input v-model="registerForm.password" type="password" placeholder="请输入密码" />
        <label>昵称</label>
        <input v-model="registerForm.name" placeholder="请输入昵称" />
        <label>手机号</label>
        <input v-model="registerForm.phone" placeholder="请输入手机号" />
        <button :disabled="loading" class="primary-btn">{{ loading ? "注册中..." : "注册并登录" }}</button>
      </form>

      <p class="error-text" v-if="error">{{ error }}</p>
    </div>
  </section>
</template>
