import { describe, expect, it } from "vitest";
import { missingPresetAttributes, prepareAttributes } from "../equipment";

describe("equipment configuration editing", () => {
  it("adds missing preset fields without overwriting custom values or order", () => {
    const existing = [
      { label: "  处理器  ", value: "Intel Core i9-14900K" },
      { label: "散热", value: "风冷" },
    ];
    const snapshot = structuredClone(existing);
    const additions = missingPresetAttributes("desktop", existing);
    expect(additions.map((field) => field.label)).not.toContain("处理器");
    expect(additions.map((field) => field.label)).toContain("显卡");
    expect(existing).toEqual(snapshot);
    expect(missingPresetAttributes("desktop", [...existing, ...additions])).toEqual([]);
  });

  it("offers fields suitable for phones and standalone operating systems", () => {
    expect(missingPresetAttributes("phone", []).map((field) => field.label)).toContain("相机");
    expect(missingPresetAttributes("system", []).map((field) => field.label)).toContain("桌面环境");
    expect(missingPresetAttributes("other", [])).toEqual([]);
  });

  it("trims entries and drops blank rows while preserving order, newlines and duplicate labels", () => {
    expect(
      prepareAttributes([
        { label: " ", value: "\n" },
        { label: " 硬盘 ", value: " KIOXIA 1 TB " },
        { label: "硬盘", value: "TOPMORE 2 TB\nNVMe SSD" },
      ]),
    ).toEqual([
      { label: "硬盘", value: "KIOXIA 1 TB" },
      { label: "硬盘", value: "TOPMORE 2 TB\nNVMe SSD" },
    ]);
  });

  it.each([
    { label: "内存", value: " " },
    { label: " ", value: "48 GB" },
  ])("rejects partly filled rows instead of losing entered data", (attribute) => {
    expect(() => prepareAttributes([attribute])).toThrow("第 1 项参数需要同时填写名称和内容");
  });
});
