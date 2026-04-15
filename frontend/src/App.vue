<template>
  <div id="app">
    <router-view v-slot="{ Component }">
      <transition
        name="fade-slide"
        mode="out-in"
      >
        <component :is="Component" />
      </transition>
    </router-view>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

onMounted(() => {
  console.log('✨ CampusZhihu App Mounted')
})
</script>

<style lang="scss">
#app {
  width: 100%;
  height: 100%;
}
</style>
