<script setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import api, { getCurrentUserId } from "../api";

const input = ref("");
const loading = ref(false);
const error = ref("");
const chatBody = ref(null);
const conversations = ref([]);
const activeConversationId = ref("");

const historyType = "order-agent";

const currentConversation = computed(() =>
  conversations.value.find((c) => c.id === activeConversationId.value)
);

function formatTime() {
  const d = new Date();
  const h = String(d.getHours()).padStart(2, "0");
  const m = String(d.getMinutes()).padStart(2, "0");
  return `${h}:${m}`;
}

function scrollToBottom() {
  nextTick(() => {
    if (chatBody.value) {
      chatBody.value.scrollTop = chatBody.value.scrollHeight;
    }
  });
}

watch(
  () => currentConversation.value?.messages || [],
  () => scrollToBottom(),
  { deep: true }
);

function conversationTime(conv) {
  if (!conv?.messages?.length) return "";
  return conv.messages[conv.messages.length - 1].time || "";
}

function addMessage(role, content) {
  const conv = currentConversation.value;
  if (!conv) return;
  conv.messages.push({
    role,
    content,
    time: formatTime()
  });
}

async function loadHistory(conversationId) {
  const conv = conversations.value.find((c) => c.id === conversationId);
  if (!conv) return;
  try {
    const resp = await api.get(`/agent/history/${historyType}/${conversationId}`);
    conv.messages = (resp.data || []).map((m) => ({
      role: m.role === "assistant" ? "assistant" : "user",
      content: m.content || "",
      time: formatTime()
    }));
    if (conv.messages.length === 0) {
      conv.messages = [{ role: "assistant", content: "你好，我是点餐助手小云。先告诉我你喜欢什么口味吧？", time: formatTime() }];
    }
  } catch (e) {
    conv.messages = [{ role: "assistant", content: "你好，我是点餐助手小云。先告诉我你喜欢什么口味吧？", time: formatTime() }];
  }
}

async function startNewConversation(isInitial = false) {
  const id = typeof self !== "undefined" && self.crypto?.randomUUID
    ? self.crypto.randomUUID()
    : `order-agent-${Date.now()}`;
  const conv = {
    id,
    title: `会话 ${conversations.value.length + 1}`,
    messages: [{ role: "assistant", content: "你好，我是点餐助手小云。先告诉我你喜欢什么口味吧？", time: formatTime() }]
  };
  conversations.value.unshift(conv);
  activeConversationId.value = id;
  if (!isInitial) {
    try {
      await api.get("/agent/history/save", {
        params: {
          type: historyType,
          conversationId: id
        }
      });
    } catch (e) {
      // ignore
    }
  }
}

async function loadConversations() {
  try {
    const ids = await api.get(`/agent/history/${historyType}`);
    if (Array.isArray(ids.data) && ids.data.length > 0) {
      conversations.value = ids.data.map((id, idx) => ({
        id,
        title: `会话 ${idx + 1}`,
        messages: []
      }));
      activeConversationId.value = conversations.value[0].id;
      await loadHistory(activeConversationId.value);
      return;
    }
  } catch (e) {
    // ignore
  }
  await startNewConversation(true);
}

async function send() {
  const text = input.value.trim();
  const conv = currentConversation.value;
  if (!text || loading.value || !conv) return;
  error.value = "";
  const userId = getCurrentUserId();
  const prompt = userId ? `用户ID=${userId}。${text}` : text;
  addMessage("user", text);
  input.value = "";
  loading.value = true;
  try {
    const resp = await api.get("/agent/chat", {
      params: {
        prompt,
        conversationId: conv.id
      }
    });
    addMessage("assistant", resp.data || "收到，但暂无响应。");
  } catch (e) {
    error.value = "点餐助手服务异常，请稍后重试。";
    addMessage("assistant", "我暂时有点忙，请稍后再试。");
  } finally {
    loading.value = false;
  }
}

async function selectConversation(id) {
  activeConversationId.value = id;
  await loadHistory(id);
}

onMounted(loadConversations);
</script>

<template>
  <section class="agent-layout">
    <aside class="agent-sidebar card">
      <div class="agent-sidebar-header">
        <h3>点餐助手会话</h3>
        <button class="ghost-btn" @click="startNewConversation">新对话</button>
      </div>
      <div class="agent-conversation-list">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="agent-conversation-item"
          :class="{ active: conv.id === activeConversationId }"
          @click="selectConversation(conv.id)"
        >
          <div class="title">{{ conv.title }}</div>
          <div class="meta">ID: {{ conv.id.slice(0, 8) }} · {{ conversationTime(conv) }}</div>
        </div>
      </div>
    </aside>

    <div class="agent-chat-fixed card" v-if="currentConversation">
      <div class="agent-chat-header">
        <div>
          <h2>订单对话机器人</h2>
          <p class="hint">告诉我口味偏好，我来推荐菜品并协助下单。</p>
        </div>
      </div>

      <div class="agent-chat-body" ref="chatBody">
        <div v-for="(m, idx) in currentConversation.messages" :key="idx" :class="['agent-msg', m.role]">
          <div class="bubble">
            {{ m.content }}
          </div>
          <div class="meta">{{ m.role === "user" ? "我" : "小云" }} · {{ m.time }}</div>
        </div>
        <div class="agent-msg assistant" v-if="loading">
          <div class="bubble">正在思考中...</div>
        </div>
      </div>

      <div class="agent-chat-footer">
        <textarea
          v-model="input"
          class="agent-textarea"
          placeholder="例如：我喜欢微辣，推荐三个销量高的饭类；再帮我下单其中两个。"
          @keydown.enter.exact.prevent="send"
        />
        <div class="agent-actions">
          <span class="error-text">{{ error }}</span>
          <button class="primary-btn" :disabled="loading || !input.trim()" @click="send">
            {{ loading ? "处理中..." : "发送" }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>
