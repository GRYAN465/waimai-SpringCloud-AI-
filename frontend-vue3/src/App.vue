<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="topbar-inner">
        <h1 class="brand">Cloud Takeout</h1>
        <nav class="menu">
          <router-link to="/menu">点餐</router-link>
          <router-link to="/agent">点餐助手</router-link>
          <router-link to="/orders">订单</router-link>
          <router-link to="/profile">个人中心</router-link>
        </nav>
      </div>
    </header>
    <main class="page-container">
      <router-view />
    </main>
  </div>
</template>
