import type { DeviceType, EquipmentAttribute, EquipmentStatus } from "../types";

export const deviceTypeOptions: { label: string; value: DeviceType }[] = [
  { label: "台式电脑", value: "desktop" },
  { label: "笔记本电脑", value: "laptop" },
  { label: "手机", value: "phone" },
  { label: "平板电脑", value: "tablet" },
  { label: "外设", value: "peripheral" },
  { label: "操作系统", value: "system" },
  { label: "其他装备", value: "other" },
];

export const statusOptions: { label: string; value: EquipmentStatus }[] = [
  { label: "使用中", value: "in-use" },
  { label: "备用", value: "standby" },
  { label: "已退役", value: "retired" },
];

const presets: Record<DeviceType, string[]> = {
  desktop: ["处理器", "显卡", "内存", "硬盘", "主板", "显示器", "操作系统", "桌面环境"],
  laptop: ["型号", "处理器", "显卡", "内存", "硬盘", "屏幕", "操作系统", "重量"],
  phone: ["型号", "芯片", "内存", "存储", "屏幕", "相机", "电池", "操作系统"],
  tablet: ["型号", "芯片", "内存", "存储", "屏幕", "手写笔", "操作系统"],
  peripheral: ["型号", "连接方式", "主要规格"],
  system: ["发行版", "版本", "内核", "桌面环境", "窗口系统", "终端", "Shell"],
  other: [],
};

export function deviceTypeLabel(type?: DeviceType): string {
  return deviceTypeOptions.find((option) => option.value === type)?.label || "其他装备";
}

/** Append missing fields so changing a preset never replaces values already entered. */
export function missingPresetAttributes(
  type: DeviceType,
  attributes: EquipmentAttribute[],
): EquipmentAttribute[] {
  const existing = new Set(attributes.map((attribute) => attribute.label.trim()));
  return presets[type]
    .filter((label) => !existing.has(label))
    .map((label) => ({ label, value: "" }));
}

/** Blank rows may be discarded, but a partly filled row must never silently lose data. */
export function prepareAttributes(attributes: EquipmentAttribute[]): EquipmentAttribute[] {
  return attributes.flatMap((attribute, index) => {
    const label = attribute.label.trim();
    const value = attribute.value.trim();
    if (!label && !value) return [];
    if (!label || !value)
      throw new Error(`第 ${index + 1} 项参数需要同时填写名称和内容，或删除该行。`);
    return [{ label, value }];
  });
}
