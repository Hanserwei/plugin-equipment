import { definePlugin } from "@halo-dev/ui-shared";
import { markRaw } from "vue";
import TablerDeviceGamepad3 from '~icons/tabler/device-gamepad-3'
import "uno.css";

export default definePlugin({
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/han-equipments",
        name: "HanEquipments",
        component: () => import("@/views/EquipmentList.vue"),
        meta: {
          permissions: ["plugin:han-equipment:view"],
          menu: {
            name: "Han 装备",
            group: "content",
            icon: markRaw(TablerDeviceGamepad3),
          },
        },
      },
    },
  ],
});
