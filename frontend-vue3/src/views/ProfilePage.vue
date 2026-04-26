<script setup>
import { onMounted, ref } from "vue";
import api, { getCurrentUserId } from "../api";

const loading = ref(false);
const message = ref("");
const form = ref({
  id: "",
  username: "",
  name: "",
  phone: "",
  tastePreference: "",
  avatar: "",
  bio: ""
});

async function loadProfile() {
  const userId = getCurrentUserId();
  if (!userId) {
    message.value = "请先登录";
    return;
  }
  loading.value = true;
  message.value = "";
  try {
    const resp = await api.get(`/users/${userId}`);
    if (resp.data.code !== 0) {
      message.value = resp.data.message || "获取用户信息失败";
      return;
    }
    Object.assign(form.value, resp.data.data || {});
  } catch (e) {
    message.value = "获取用户信息失败";
  } finally {
    loading.value = false;
  }
}

async function saveProfile() {
  const userId = getCurrentUserId();
  if (!userId) {
    message.value = "请先登录";
    return;
  }
  loading.value = true;
  message.value = "";
  try {
    const resp = await api.put(`/users/profile/${userId}`, {
      name: form.value.name,
      phone: form.value.phone,
      tastePreference: form.value.tastePreference,
      avatar: form.value.avatar,
      bio: form.value.bio
    });
    if (resp.data.code !== 0) {
      message.value = resp.data.message || "保存失败";
      return;
    }
    message.value = "保存成功";
    Object.assign(form.value, resp.data.data || {});
  } catch (e) {
    message.value = "保存失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}

onMounted(loadProfile);
</script>

<template>
  <section>
    <h2>个人中心</h2>
    <p class="hint">可查看并修改昵称、手机号、口味偏好、头像地址和个人简介。</p>

    <form class="card form-grid profile-form" @submit.prevent="saveProfile">
      <label>用户ID</label>
      <input :value="form.id" disabled />
      <label>用户名</label>
      <input :value="form.username" disabled />
      <label>昵称</label>
      <input v-model="form.name" />
      <label>手机号</label>
      <input v-model="form.phone" />
      <label>口味偏好</label>
      <input v-model="form.tastePreference" placeholder="例如：微辣、偏爱米饭类" />
      <label>头像URL</label>
      <input v-model="form.avatar" />
      <label>个人简介</label>
      <textarea rows="3" v-model="form.bio" />
      <button class="primary-btn" :disabled="loading">{{ loading ? "保存中..." : "保存资料" }}</button>
    </form>
    <p class="error-text" v-if="message">{{ message }}</p>
  </section>
</template>
