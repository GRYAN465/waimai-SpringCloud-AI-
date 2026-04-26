<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import api, { getCurrentUserId } from "../api";

const router = useRouter();
const products = ref([]);
const loading = ref(false);
const message = ref("");
const counts = ref({});

function logout() {
  localStorage.clear();
  router.push("/auth");
}

async function loadProducts() {
  loading.value = true;
  message.value = "";
  try {
    const resp = await api.get("/products");
    if (resp.data.code !== 0) {
      message.value = resp.data.message || "加载菜品失败";
      return;
    }
    products.value = resp.data.data || [];
    for (const p of products.value) {
      if (!counts.value[p.id]) {
        counts.value[p.id] = 1;
      }
    }
  } catch (e) {
    message.value = "加载菜品失败，请检查后端服务";
  } finally {
    loading.value = false;
  }
}

async function placeOrder(product) {
  const userId = getCurrentUserId();
  if (!userId) {
    message.value = "用户信息丢失，请重新登录";
    router.push("/auth");
    return;
  }
  const count = Number(counts.value[product.id] || 1);
  try {
    const resp = await api.post("/orders", {
      userId,
      productId: product.id,
      count,
      remark: "网页下单"
    });
    if (resp.data.code !== 0) {
      message.value = resp.data.message || "下单失败";
      return;
    }
    message.value = `下单成功，订单号：${resp.data.data.id}`;
    router.push("/orders");
  } catch (e) {
    message.value = "下单失败，请检查订单服务日志";
  }
}

onMounted(loadProducts);
</script>

<template>
  <section>
    <div class="section-header">
      <h2>今日推荐</h2>
      <button class="ghost-btn" @click="logout">退出登录</button>
    </div>
    <p class="hint">主题色：青绿色 + 白色。选择菜品后可直接下单。</p>

    <p class="error-text" v-if="message">{{ message }}</p>
    <p v-if="loading">加载中...</p>

    <div class="grid" v-else>
      <article class="card dish-card" v-for="item in products" :key="item.id">
        <h3>{{ item.name }}</h3>
        <p class="price">¥{{ item.price }}</p>
        <p>辣度：{{ item.spicyLevel || "不辣" }}</p>
        <p>销量：{{ item.sales || 0 }}</p>
        <p class="muted">{{ item.description || "暂无描述" }}</p>
        <div class="order-row">
          <input type="number" min="1" v-model="counts[item.id]" />
          <button class="primary-btn" @click="placeOrder(item)">下单</button>
        </div>
      </article>
    </div>
  </section>
</template>
