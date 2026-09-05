export interface Metadata {
  name: string;
  generateName?: string;
  labels?: {
    [key: string]: string;
  } | null;
  annotations?: {
    [key: string]: string;
  } | null;
  version?: number | null;
  creationTimestamp?: string | null;
  deletionTimestamp?: string | null;
}

export interface EquipmentGroupSpec {
  displayName: string;
  description?: string;
  priority?: number;
}

export interface EquipmentGroupStatus {
  equipmentCount: number;
}

export type DeviceType =
  "desktop" | "laptop" | "phone" | "tablet" | "peripheral" | "system" | "other";
export type EquipmentStatus = "in-use" | "standby" | "retired";

export interface EquipmentAttribute {
  label: string;
  value: string;
}

export interface EquipmentSpec {
  displayName: string;
  deviceType?: DeviceType;
  status?: EquipmentStatus;
  featured?: boolean;
  attributes?: EquipmentAttribute[];
  specification?: string;
  description?: string;
  url?: string;
  cover?: string;
  priority?: number;
  groupName: string;
}

export interface Equipment {
  spec: EquipmentSpec;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
}

export interface EquipmentGroup {
  spec: EquipmentGroupSpec;
  apiVersion: string;
  kind: string;
  metadata: Metadata;
  status: EquipmentGroupStatus;
}

export interface EquipmentList {
  page: number;
  size: number;
  total: number;
  totalPages: number;
  items: Array<Equipment>;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface EquipmentGroupList {
  page: number;
  size: number;
  total: number;
  items: Array<EquipmentGroup>;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}
