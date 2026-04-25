<script setup>
import { ref } from "vue";
import axios from "axios";

const user = ref(null);
const product = ref(null);
const orderResult = ref(null);
const loading = ref(false);
const error = ref("");
const token = ref("");

async function login() {
  error.value = "";
  loading.value = true;
  try {
    const resp = await axios.post("/api/auth/login", {
      username: "demo",
      password: "123456"
    });
    token.value = resp.data.data.token;
  } catch (e) {
    error.value = "登录失败，请检查 auth 服务是否启动";
  } finally {
    loading.value = false;
  }
}

function authHeaders() {
  if (!token.value) {
    return {};
  }
  return {
    Authorization: `Bearer ${token.value}`
  };
}

async function loadData() {
  if (!token.value) {
    error.value = "请先登录";
    return;
  }
  error.value = "";
  loading.value = true;
  try {
    const userResp = await axios.get("/api/users/1", { headers: authHeaders() });
    const productResp = await axios.get("/api/products/1", { headers: authHeaders() });
    user.value = userResp.data.data;
    product.value = productResp.data.data;
  } catch (e) {
    error.value = "加载数据失败，请确认 token 和后端服务";
  } finally {
    loading.value = false;
  }
}

async function placeOrder() {
  if (!token.value) {
    error.value = "请先登录";
    return;
  }
  error.value = "";
  loading.value = true;
  try {
    const resp = await axios.post(
      "/api/orders",
      {
        userId: 1,
        productId: 1,
        count: 2
      },
      { headers: authHeaders() }
    );
    orderResult.value = resp.data.data;
  } catch (e) {
    error.value = "下单失败，请检查网关和订单服务日志";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main style="max-width: 720px; margin: 32px auto; font-family: Arial, sans-serif">
    <h2>微服务外卖演示 (Vue3)</h2>
    <p>流程：登录拿JWT -> 查询用户/商品 -> 下单 -> 发布MQ消息</p>

    <div style="display: flex; gap: 12px; margin-bottom: 12px;">
      <button @click="login" :disabled="loading">登录(获取JWT)</button>
      <button @click="loadData" :disabled="loading">加载基础数据</button>
      <button @click="placeOrder" :disabled="loading">下单(2份)</button>
    </div>

    <div v-if="token">
      <h3>JWT(已获取)</h3>
      <pre>{{ token }}</pre>
    </div>

    <p v-if="error" style="color: #c0392b">{{ error }}</p>

    <div v-if="user">
      <h3>用户</h3>
      <pre>{{ user }}</pre>
    </div>

    <div v-if="product">
      <h3>商品</h3>
      <pre>{{ product }}</pre>
    </div>

    <div v-if="orderResult">
      <h3>下单结果</h3>
      <pre>{{ orderResult }}</pre>
    </div>
  </main>
</template>
