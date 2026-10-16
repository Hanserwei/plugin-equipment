<script setup lang="ts">
import type { DeviceType } from "@/types";
defineProps<{ type?: DeviceType }>();
</script>

<template>
  <svg
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <g v-if="type === 'desktop'">
      <rect x="3" y="3" width="18" height="13" rx="2" />
      <path d="M8 21h8m-4-5v5" />
    </g>
    <g v-else-if="type === 'laptop'">
      <rect x="4" y="3" width="16" height="13" rx="2" />
      <path d="M4 16 2 20h20l-2-4" />
    </g>
    <g v-else-if="type === 'phone'">
      <rect x="6" y="2" width="12" height="20" rx="3" />
      <path d="M10 18h4" />
    </g>
    <g v-else-if="type === 'tablet'">
      <rect x="3" y="2" width="18" height="20" rx="3" />
      <path d="M10 18h4" />
    </g>
    <g v-else-if="type === 'system'">
      <rect x="2" y="3" width="20" height="18" rx="3" />
      <path d="m6 8 4 4-4 4m7 0h5" />
    </g>
    <g v-else-if="type === 'peripheral'">
      <rect x="6" y="2" width="12" height="20" rx="6" />
      <path d="M12 6v4" />
    </g>
    <g v-else><path d="m12 3 9 5-9 5-9-5 9-5Zm-9 9 9 5 9-5M3 16l9 5 9-5" /></g>
  </svg>
</template>
