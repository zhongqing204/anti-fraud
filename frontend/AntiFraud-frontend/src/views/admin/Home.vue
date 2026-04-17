<template>
</template>

<script setup>

import {reactive} from "vue";


const data = reactive({
  user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
  noticeData: []
})

</script>