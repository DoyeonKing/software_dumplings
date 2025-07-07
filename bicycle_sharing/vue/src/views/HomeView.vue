
<template>
  <div>
    欢迎
  </div>
    
  <div>
    {{a}}
    <RouterLink to="/">跳转 </RouterLink>
  </div>

</template>

<script setup>
import {reactive,ref} from "vue"
const a = ref(1)

</script>
