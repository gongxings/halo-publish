<template>
  <div class="p-6">
    <h2 class="text-xl font-bold mb-4">同步配置</h2>
    <div class="space-y-2">
      <div v-for="(v, k) in form" :key="k" class="flex flex-col">
        <label class="text-sm text-gray-500">{{ k }}</label>
        <input v-model="form[k]" class="border p-2 rounded" />
      </div>
    </div>
    <button class="mt-4 bg-blue-500 text-white px-4 py-2 rounded" @click="save">保存</button>
  </div>
</template>
<script setup>
import { reactive } from 'vue';
import axios from 'axios';
const form = reactive({ wechatAppId: '', wechatAppSecret: '', csdnToken: '', juejinCookie: '' });
async function save() {
  await axios.post('/api/plugins/upage-sync/config', form);
  alert('已保存');
}
</script>
