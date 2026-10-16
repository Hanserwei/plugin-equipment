import { afterEach, describe, expect, it, vi } from "vitest";
import { flushPromises, mount, type VueWrapper } from "@vue/test-utils";
import { createInput, defaultConfig, plugin } from "@formkit/vue";
import { defineComponent, h } from "vue";
import { axiosInstance } from "@halo-dev/api-client";
import EquipmentEditingModal from "../EquipmentEditingModal.vue";
import type { Equipment } from "../../types";

vi.mock("@halo-dev/api-client", () => ({ axiosInstance: { post: vi.fn(), put: vi.fn() } }));
vi.mock("@halo-dev/components", async () => {
  const { defineComponent, h } = await import("vue");
  return {
    Toast: { info: vi.fn(), error: vi.fn() },
    VModal: defineComponent({
      setup(_, { slots, expose }) {
        expose({ close: vi.fn() });
        return () => h("div", [slots.default?.(), slots.footer?.()]);
      },
    }),
    VSpace: defineComponent({
      setup(_, { slots }) {
        return () => h("div", slots.default?.());
      },
    }),
    VButton: defineComponent({
      setup(_, { slots }) {
        return () => h("button", { type: "button" }, slots.default?.());
      },
    }),
  };
});

const attachment = createInput(
  defineComponent({
    props: ["context"],
    setup: () => () => h("input", { type: "text", "aria-label": "设备图片" }),
  }),
);
const annotations = defineComponent({
  props: ["value"],
  setup(props, { expose }) {
    expose({ handleSubmit: vi.fn(), annotations: props.value || {}, customAnnotations: {} });
    return () => h("div");
  },
});

let wrapper: VueWrapper;
function open(equipment?: Equipment) {
  wrapper = mount(EquipmentEditingModal, {
    attachTo: document.body,
    props: { equipment, group: "daily" },
    global: {
      plugins: [[plugin, defaultConfig({ config: { delay: 0 }, inputs: { attachment } })]],
      components: { AnnotationsForm: annotations },
    },
  });
  return wrapper;
}
async function settle() {
  await new Promise((resolve) => setTimeout(resolve, 25));
  await flushPromises();
}
async function click(text: string) {
  const button = wrapper.findAll("button").find((item) => item.text() === text);
  expect(button).toBeDefined();
  await button!.trigger("click");
  await settle();
}
function equipment(name: string): Equipment {
  return {
    apiVersion: "equipment.hanserwei.github.io/v1alpha1",
    kind: "Equipment",
    metadata: { name, version: 3, annotations: { note: "keep" } },
    spec: {
      displayName: name,
      deviceType: "desktop",
      groupName: "daily",
      status: "in-use",
      featured: true,
      attributes: [
        { label: "处理器", value: "i9-14900K" },
        { label: "显卡", value: "RTX 3060" },
      ],
    },
  };
}

afterEach(() => {
  wrapper?.unmount();
  document.body.innerHTML = "";
  vi.clearAllMocks();
});

describe("equipment editor with real FormKit", () => {
  it("creates a device with custom fields, preserving its selected group", async () => {
    open();
    await settle();
    await wrapper.get('input[name="displayName"]').setValue("主力电脑");
    await click("添加自定义参数");
    await wrapper.get(".equipment-attribute-row input").setValue("处理器");
    await wrapper.get(".equipment-attribute-row textarea").setValue("Intel Core i9-14900K");
    vi.mocked(axiosInstance.post).mockResolvedValue({ data: equipment("created") });
    await click("保存");
    expect(axiosInstance.post).toHaveBeenCalledOnce();
    expect(vi.mocked(axiosInstance.post).mock.calls[0][1]).toMatchObject({
      spec: {
        displayName: "主力电脑",
        groupName: "daily",
        deviceType: "desktop",
        attributes: [{ label: "处理器", value: "Intel Core i9-14900K" }],
      },
    });
  });

  it("saves reordered and removed rows without changing the original object or metadata", async () => {
    const original = equipment("desktop");
    open(original);
    await settle();
    await wrapper.get('[aria-label="上移第 2 项"]').trigger("click");
    await wrapper.get('[aria-label="删除第 2 项"]').trigger("click");
    vi.mocked(axiosInstance.put).mockResolvedValue({ data: original });
    await click("保存");
    expect(axiosInstance.put).toHaveBeenCalledOnce();
    expect(vi.mocked(axiosInstance.put).mock.calls[0][1]).toMatchObject({
      metadata: { version: 3, annotations: { note: "keep" } },
      spec: { featured: true, attributes: [{ label: "显卡", value: "RTX 3060" }] },
    });
    expect(original.spec.attributes).toHaveLength(2);
  });

  it("blocks incomplete parameters and reloads state when navigating to another device", async () => {
    open(equipment("first"));
    await settle();
    await wrapper.get(".equipment-attribute-row textarea").setValue("");
    await click("保存");
    expect(wrapper.get('[role="alert"]').text()).toContain("第 1 项");
    expect(axiosInstance.put).not.toHaveBeenCalled();
    const next = equipment("second");
    next.spec.attributes = [{ label: "操作系统", value: "Arch Linux" }];
    await wrapper.setProps({ equipment: next });
    await settle();
    expect(wrapper.findAll(".equipment-attribute-row")).toHaveLength(1);
    expect(
      (wrapper.get(".equipment-attribute-row textarea").element as HTMLTextAreaElement).value,
    ).toBe("Arch Linux");
    expect(wrapper.find('[role="alert"]').exists()).toBe(false);
  });
});
