// Keep this an ambient declaration; env.d.ts is a module for library augmentations.
declare module "*.vue" {
  import type { ComponentOptions } from "vue";
  const component: ComponentOptions;
  export default component;
}
