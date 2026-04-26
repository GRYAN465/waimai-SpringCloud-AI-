<script setup>
import { onMounted, ref } from "vue";
import api, { getCurrentUserId } from "../api";

const loading = ref(false);
const error = ref("");
const orders = ref([]);

async function loadOrders() {
  const userId = getCurrentUserId();
  if (!userId) {
    error.value = "请先登录";
    return;
  }
  loading.value = true;
  error.value = "";
  try {
    const resp = await api.get(`/orders/user/${userId}`);
    if (resp.data.code !== 0) {
      error.value = resp.data.message || "获取订单失败";
      return;
    }
    orders.value = resp.data.data || [];
  } catch (e) {
    error.value = "获取订单失败，请检查订单服务";
  } finally {
    loading.value = false;
  }
}

onMounted(loadOrders);
</script>

<template>
  <section>
    <div class="section-header">
      <h2>我的订单</h2>
      <button class="ghost-btn" @click="loadOrders">刷新</button>
    </div>

    <p v-if="loading">加载中...</p>
    <p class="error-text" v-if="error">{{ error }}</p>

    <div v-if="!loading && orders.length === 0" class="card">暂无订单，快去点餐吧。</div>

    <div class="grid orders-grid" v-else>
      <article class="card" v-for="o in orders" :key="o.id">
        <h3>订单 #{{ o.id }}</h3>
        <p>用户ID：{{ o.userId }}</p>
        <p>菜品ID：{{ o.productId }}</p>
        <p>数量：{{ o.count }}</p>
        <p>总价：¥{{ o.totalAmount }}</p>
        <p>状态：{{ o.status }}</p>
        <p>下单时间：{{ o.orderTime }}</p>
        <p class="muted">备注：{{ o.remark || "无" }}</p>
      </article>
    </div>
  </section>
</template>
