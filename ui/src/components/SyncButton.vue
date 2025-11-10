<template>
  <button class="bg-green-500 text-white px-3 py-1 rounded" @click="sync">
    一键同步
  </button>
</template>

<script setup>
import axios from 'axios';
const props = defineProps({ articleId: Number });
async function sync() {
  await axios.post(`/api/plugins/upage-sync/publish/${props.articleId}?platform=ALL`);
  alert('已触发同步任务');
}
</script>
