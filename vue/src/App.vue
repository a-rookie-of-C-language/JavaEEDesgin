<template>
  <div id="app">
    <StudentManagement />
  </div>
</template>

<script setup>
import StudentManagement from './components/StudentManagement.vue'
</script>

<style>
#app {
  min-height: 100vh;
  background-color: #f0f2f5;
}
</style>

<style scoped>
header {
  line-height: 1.5;
}

.logo {
  display: block;
  margin: 0 auto 2rem;
}

@media (min-width: 1024px) {
  header {
    display: flex;
    place-items: center;
    padding-right: calc(var(--section-gap) / 2);
  }

  .logo {
    margin: 0 2rem 0 0;
  }

  header .wrapper {
    display: flex;
    place-items: flex-start;
    flex-wrap: wrap;
  }
}
</style>
