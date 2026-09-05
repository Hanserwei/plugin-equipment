<script lang="ts" setup>
import type { Equipment, EquipmentAttribute } from "@/types";
import {
  deviceTypeOptions,
  missingPresetAttributes,
  prepareAttributes,
  statusOptions,
} from "@/utils/equipment";
import { axiosInstance } from "@halo-dev/api-client";
import { Toast, VSpace, VButton, VModal } from "@halo-dev/components";
import { cloneDeep } from "lodash-es";
import { computed, nextTick, ref, useTemplateRef, watch } from "vue";

const props = withDefaults(defineProps<{ equipment?: Equipment; group?: string }>(), {
  equipment: undefined,
  group: undefined,
});
const emit = defineEmits<{
  (event: "close"): void;
  (event: "saved", equipment: Equipment): void;
}>();

const initialFormState: Equipment = {
  metadata: { name: "", generateName: "equipment-" },
  spec: {
    displayName: "",
    cover: "",
    deviceType: "desktop",
    status: "in-use",
    featured: false,
    groupName: props.group || "",
  },
  kind: "Equipment",
  apiVersion: "equipment.kunkunyu.com/v1alpha1",
};

type AttributeRow = EquipmentAttribute & { id: number };
let rowId = 0;
const attributes = ref<AttributeRow[]>([]);
const attributeError = ref("");
const formState = ref<Equipment>(cloneDeep(initialFormState));
const isSubmitting = ref(false);
const modal = useTemplateRef<InstanceType<typeof VModal> | null>("modal");
const annotationsFormRef = ref();
const isUpdateMode = computed(() => !!formState.value.metadata.name);
const modalTitle = computed(() => (isUpdateMode.value ? "编辑装备" : "添加装备"));

watch(
  () => props.equipment,
  (equipment) => {
    formState.value = cloneDeep(equipment || initialFormState);
    attributes.value = (equipment?.spec.attributes || []).map((attribute) => ({
      ...attribute,
      id: rowId++,
    }));
    attributeError.value = "";
  },
  { immediate: true },
);

function addAttribute() {
  attributes.value.push({ id: rowId++, label: "", value: "" });
}

function applyPreset() {
  const fields = missingPresetAttributes(
    formState.value.spec.deviceType || "other",
    attributes.value,
  );
  attributes.value.push(...fields.map((field) => ({ ...field, id: rowId++ })));
  if (!fields.length) Toast.info("该类型的预设参数已添加，你也可以添加自定义参数。");
}

function moveAttribute(index: number, direction: number) {
  const destination = index + direction;
  if (destination < 0 || destination >= attributes.value.length) return;
  const [row] = attributes.value.splice(index, 1);
  attributes.value.splice(destination, 0, row);
}

async function handleSaveEquipment() {
  if (isSubmitting.value) return;
  attributeError.value = "";
  let preparedAttributes: EquipmentAttribute[];
  try {
    preparedAttributes = prepareAttributes(attributes.value);
  } catch (error) {
    attributeError.value = (error as Error).message;
    return;
  }
  annotationsFormRef.value?.handleSubmit();
  await nextTick();
  const { customAnnotations, annotations, customFormInvalid, specFormInvalid } =
    annotationsFormRef.value || {};
  if (customFormInvalid || specFormInvalid) return;

  const equipment = cloneDeep(formState.value);
  equipment.spec.attributes = preparedAttributes;
  equipment.metadata.annotations = { ...annotations, ...customAnnotations };
  if (!isUpdateMode.value && props.group) equipment.spec.groupName = props.group;

  try {
    isSubmitting.value = true;
    const { data } = isUpdateMode.value
      ? await axiosInstance.put<Equipment>(
          `/apis/equipment.kunkunyu.com/v1alpha1/equipments/${equipment.metadata.name}`,
          equipment,
        )
      : await axiosInstance.post<Equipment>(
          "/apis/equipment.kunkunyu.com/v1alpha1/equipments",
          equipment,
        );
    emit("saved", data);
    modal.value?.close();
  } catch (error) {
    console.error(error);
    Toast.error("保存失败，请检查填写内容后重试。");
  } finally {
    isSubmitting.value = false;
  }
}
</script>

<template>
  <VModal ref="modal" :title="modalTitle" :width="880" @close="emit('close')">
    <template #actions><slot name="append-actions" /></template>
    <FormKit
      id="equipment-form"
      v-model="formState.spec"
      name="equipment-form"
      :actions="false"
      :config="{ validationVisibility: 'submit' }"
      type="form"
      @submit="handleSaveEquipment"
    >
      <section class="equipment-editor-section">
        <div class="equipment-editor-heading">
          <h3>设备信息</h3>
          <p>用一句话介绍它，再用参数表展示具体配置。</p>
        </div>
        <div class="equipment-editor-grid">
          <FormKit
            name="displayName"
            label="装备名称"
            type="text"
            validation="required"
            placeholder="例如：我的主力工作站"
          />
          <FormKit
            name="deviceType"
            label="设备类型"
            type="select"
            :options="deviceTypeOptions"
            validation="required"
          />
          <FormKit name="status" label="使用状态" type="select" :options="statusOptions" />
          <FormKit
            name="specification"
            label="简短概述"
            type="text"
            placeholder="例如：开发、创作与日常游戏"
          />
        </div>
        <FormKit
          name="description"
          label="使用感受"
          type="textarea"
          placeholder="它在你的日常生活中扮演什么角色？"
        />
        <FormKit
          name="cover"
          label="设备图片（可选）"
          type="attachment"
          :accepts="['image/*']"
          help="可以使用实拍图或产品图；未设置图片时展示设备类型图标。"
        />
        <FormKit
          name="url"
          label="相关链接（可选）"
          type="text"
          placeholder="产品官网、介绍文章或你的装机记录"
        />
        <FormKit
          name="featured"
          label="重点展示"
          type="checkbox"
          help="在支持的模板中使用宽卡片，适合主力电脑或配置较多的设备。"
        />
      </section>

      <section class="equipment-editor-section">
        <div class="equipment-editor-heading">
          <h3>
            配置参数 <span>{{ attributes.length }}</span>
          </h3>
          <p>参数名称可以自由修改。按展示顺序填写；不需要的模板字段可以删除。</p>
        </div>
        <div class="equipment-editor-actions">
          <VButton size="sm" @click="applyPreset">添加类型预设参数</VButton>
          <VButton size="sm" @click="addAttribute">添加自定义参数</VButton>
        </div>
        <p v-if="!attributes.length" class="equipment-editor-empty">
          还没有配置参数。选择上方设备类型后，可一键添加常用字段。
        </p>
        <div
          v-for="(attribute, index) in attributes"
          :key="attribute.id"
          class="equipment-attribute-row"
        >
          <label>
            <span class="equipment-editor-sr-only">第 {{ index + 1 }} 项参数名称</span>
            <input v-model="attribute.label" type="text" placeholder="参数名称，如处理器" />
          </label>
          <label>
            <span class="equipment-editor-sr-only">第 {{ index + 1 }} 项参数内容</span>
            <textarea
              v-model="attribute.value"
              rows="2"
              placeholder="参数内容，如 Intel Core i9-14900K"
            />
          </label>
          <div class="equipment-attribute-actions">
            <button
              type="button"
              :disabled="index === 0"
              :aria-label="`上移第 ${index + 1} 项`"
              title="上移"
              @click="moveAttribute(index, -1)"
            >
              ↑
            </button>
            <button
              type="button"
              :disabled="index === attributes.length - 1"
              :aria-label="`下移第 ${index + 1} 项`"
              title="下移"
              @click="moveAttribute(index, 1)"
            >
              ↓
            </button>
            <button
              type="button"
              :aria-label="`删除第 ${index + 1} 项`"
              title="删除"
              @click="attributes.splice(index, 1)"
            >
              删除
            </button>
          </div>
        </div>
        <p v-if="attributeError" class="equipment-editor-error" role="alert">
          {{ attributeError }}
        </p>
      </section>
    </FormKit>

    <details class="equipment-editor-metadata">
      <summary>高级：元数据</summary>
      <AnnotationsForm
        :key="formState.metadata.name"
        ref="annotationsFormRef"
        :value="formState.metadata.annotations"
        kind="Equipment"
        group="equipment.kunkunyu.com"
      />
    </details>
    <template #footer>
      <VSpace>
        <VButton :loading="isSubmitting" type="secondary" @click="$formkit.submit('equipment-form')"
          >保存</VButton
        >
        <VButton @click="modal?.close()">取消</VButton>
      </VSpace>
    </template>
  </VModal>
</template>

<style scoped>
.equipment-editor-section + .equipment-editor-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #e5e7eb;
}
.equipment-editor-heading {
  margin-bottom: 18px;
}
.equipment-editor-heading h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}
.equipment-editor-heading h3 span {
  margin-left: 6px;
  color: #6b7280;
  font-size: 12px;
}
.equipment-editor-heading p,
.equipment-editor-empty {
  margin: 6px 0 0;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.7;
}
.equipment-editor-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 20px;
}
.equipment-editor-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.equipment-editor-empty {
  border: 1px dashed #d1d5db;
  border-radius: 8px;
  padding: 24px;
  text-align: center;
}
.equipment-attribute-row {
  display: grid;
  grid-template-columns: minmax(100px, 1fr) minmax(0, 2.4fr) auto;
  align-items: start;
  gap: 10px;
  margin-bottom: 10px;
}
.equipment-attribute-row label {
  min-width: 0;
}
.equipment-attribute-row input,
.equipment-attribute-row textarea {
  box-sizing: border-box;
  width: 100%;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 8px 10px;
  font: inherit;
  font-size: 13px;
  line-height: 1.5;
  background: #fff;
  color: #111827;
}
.equipment-attribute-row textarea {
  resize: vertical;
  min-height: 38px;
}
.equipment-attribute-row input:focus,
.equipment-attribute-row textarea:focus {
  outline: 2px solid #6366f1;
  outline-offset: 1px;
}
.equipment-attribute-actions {
  display: flex;
  gap: 4px;
  padding-top: 5px;
}
.equipment-attribute-actions button {
  border: 0;
  border-radius: 4px;
  padding: 4px 6px;
  background: #f3f4f6;
  color: #374151;
  cursor: pointer;
  font-size: 12px;
}
.equipment-attribute-actions button:disabled {
  opacity: 0.35;
  cursor: default;
}
.equipment-attribute-actions button:focus-visible {
  outline: 2px solid #6366f1;
  outline-offset: 2px;
}
.equipment-editor-error {
  color: #b91c1c;
  font-size: 13px;
}
.equipment-editor-metadata {
  margin-top: 24px;
  border-top: 1px solid #e5e7eb;
  padding-top: 16px;
}
.equipment-editor-metadata summary {
  margin-bottom: 16px;
  cursor: pointer;
  color: #6b7280;
  font-size: 13px;
}
.equipment-editor-sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  overflow: hidden;
  clip-path: inset(50%);
  white-space: nowrap;
}
@media (max-width: 640px) {
  .equipment-editor-grid {
    grid-template-columns: minmax(0, 1fr);
  }
  .equipment-attribute-row {
    grid-template-columns: minmax(0, 1fr) minmax(0, 2fr);
  }
  .equipment-attribute-actions {
    grid-column: 1 / -1;
    justify-content: flex-end;
    padding-top: 0;
  }
}
</style>
