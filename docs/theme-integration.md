# Hanlo 装备页适配指南

`han-equipment` 2.0.0 要求 **Halo 2.26.0 或更新版本**。本文说明主题接入方式；插件不会自动修改主题源码。

## 为什么主题需要适配

插件通过 `EquipmentRouter` 注册 `/equipments`。Halo 优先使用当前主题的 `equipments.html`，主题未提供时才使用插件内置页面。

当前 Hanlo 模板只读取 `displayName`、`cover`、`specification`、`description`、`url`，并把规格限制为单行、描述限制为三行。更新插件后，后台能保存结构化参数，但这个旧模板不会自动展示新字段。

在 Hanlo 主题根目录编辑 `src/equipments.html`。

修改 `src/` 源文件，再通过主题构建生成 `templates/`；不要只修改生成目录。

## 推荐方案：复用插件卡片，只改两处

主题继续负责原来的导航、横幅、页面外壳和页脚。插件提供装备卡片与样式，无需复制一套配置展示代码到主题。

### 1. 在 head 片段引入卡片样式

打开 `src/equipments.html`，在现有 `<th:block th:fragment="head">` 内增加：

```html
<link rel="stylesheet"
      th:href="@{/plugins/han-equipment/assets/static/equipment.css(v='2.0.0')}">
```

原有 Open Graph 片段继续保留。`v` 用于更新缓存；以后插件样式更新时同步改为对应版本。

样式只针对 `.equipment-showcase` 及带有 `equipment-` 前缀的元素，读取 Hanlo 的 `--heo-card-bg`、`--heo-fontcolor`、`--heo-secondtext`、`--heo-main` 等变量。深色模式支持 `data-theme="dark"` 等常见标记。

### 2. 替换旧装备列表

找到这一整块旧列表，从开始标签到它对应的结束标签全部替换：

```html
<div id="equipment" th:if="${not #lists.isEmpty(groups)}">
    <!-- 原来的 group/equipment 循环和 equipment-item-content-item 卡片 -->
</div>
```

替换为：

```html
<th:block th:replace="~{plugin:han-equipment:modules/equipment :: list(groups=${groups})}"></th:block>
```

这个片段消费 `/equipments` 路由已提供的 `groups`，不需要增加浏览器 API 请求或 JavaScript。

替换后的页面主体位置如下，原有页面布局保持自己的写法即可：

```html
<div id="page">
    <!-- 保留原来的 macro/author-content 横幅 -->
    <div th:replace="~{macro/author-content :: author-content(
        background=${theme.config.equipment.backgroundImg},
        smallTitle=${theme.config.equipment.smallTitle},
        bigTitle=${theme.config.equipment.bigTitle},
        detail=${theme.config.equipment.detail},
        buttonUrl='',
        buttonTitle='')}"></div>

    <th:block th:replace="~{plugin:han-equipment:modules/equipment :: list(groups=${groups})}"></th:block>
</div>
```

**不需要修改 `src/css/pages/content-tools.css`。** 其中旧的 `.equipment-item-content-item-*` 选择器不会匹配新卡片，可留待主题后续清理。

### 构建与安装顺序

1. 将 Halo 升级到满足插件要求的版本，停用旧 `equipment` 插件，然后安装本项目构建的 `han-equipment-2.0.0.jar` 并启用。两个插件共享 `/equipments` 路由，不应同时启用。
2. 在 Halo Console 的“Han 装备”中创建分组，再填写装备信息和参数。
3. 按以上两处修改主题，执行主题已有的 `pnpm build`，生成并安装新的主题包。
4. `/equipments` 会使用主题外壳和插件新卡片。仅更新插件、不改主题模板时，旧主题仍然只显示原有字段。

如果此前已经接入 `plugin:equipment:...` 或 `/plugins/equipment/...`，需要将插件名称段替换为 `han-equipment`。新的 API 分组是 `equipment.hanserwei.github.io`，不会自动读取旧插件的数据。

## 新增字段与模板契约

单个装备仍是 `equipment.spec`，详细配置通过有序数组 `attributes` 表达。

| 字段 | 类型 / 可选值 | 用途 |
| --- | --- | --- |
| `displayName` | 字符串 | 装备名称 |
| `deviceType` | `desktop`、`laptop`、`phone`、`tablet`、`peripheral`、`system`、`other` | 设备类型与默认图标 |
| `status` | `in-use`、`standby`、`retired` | 使用中、备用、已退役 |
| `featured` | 布尔值 | 重点展示的宽卡片 |
| `specification` | 字符串 | 简短概述，如“开发与日常游戏” |
| `description` | 字符串 | 使用感受，保留换行 |
| `attributes` | `{ label: string, value: string }[]` | 参数按数组顺序展示，内容允许换行、名称允许重复 |
| `cover` | 可选图片地址 | 不填时展示设备类型图标 |
| `url` | 可选链接 | 产品详情、介绍文章等 |
| `groupName` | 分组资源名称 | 所属分组 |

在自定义页面主动查询时使用 `hanEquipmentFinder`，例如 `${hanEquipmentFinder.groupBy()}`。`/equipments` 页面直接使用路由提供的 `groups` 即可。

Finder / 路由返回的 `EquipmentVo` 另提供三个展示属性：

- `equipment.deviceTypeLabel`：中文设备类型。
- `equipment.statusLabel`：中文状态；未设置时为空字符串。
- `equipment.link`：可展示的 HTTP(S) 链接或 `/` 开头的站内路径；无效或其他协议返回 `null`。内置模板使用此值，外链带 `noopener noreferrer`。

可复用的模板片段：

```html
<!-- 完整分组列表，含空状态 -->
<th:block th:replace="~{plugin:han-equipment:modules/equipment :: list(groups=${groups})}"></th:block>

<!-- 单张卡片，需要置于 .equipment-showcase 内，使用同一份 CSS -->
<th:block th:replace="~{plugin:han-equipment:modules/card :: card(equipment=${equipment})}"></th:block>

<!-- 单个类型图标 -->
<th:block th:replace="~{plugin:han-equipment:modules/icon :: icon(type=${equipment.spec.deviceType})}"></th:block>
```

## 如果希望由主题独立设计卡片

可以继续使用 `groups → group.equipments` 循环，读取以上字段。参数表最小写法如下：

```html
<dl th:if="${not #lists.isEmpty(equipment.spec.attributes)}">
    <div th:each="attribute : ${equipment.spec.attributes}">
        <dt th:text="${attribute.label}"></dt>
        <dd th:text="${attribute.value}"></dd>
    </div>
</dl>
```

使用 `th:text` 转义内容，不使用 `th:utext`。参数内容设置 `white-space: pre-line; overflow-wrap: anywhere`，卡片取消原有的固定描述高度、单行截断和绝对定位底栏；通过 `equipment.spec.featured == true` 决定是否跨列。以上处理已包含在推荐的插件卡片中。

## 本机配置示例

[desktop.json](examples/desktop.json) 是按本机 `fastfetch --format json` 结果整理的资源示例，显存由 `nvidia-smi` 补充。它不是自动导入任务，也没有写入任何 Halo 实例。

- i9-14900K，24 核心 / 32 线程，最高 6.0 GHz。
- RTX 3060，12 GB 显存。
- 系统识别内存 46.79 GiB。fastfetch 无法读取 SMBIOS 内存条信息，未推断内存条型号、频率或标称容量。
- Colorful iGame Z790D5 FLOW V20 主板。
- KIOXIA EXCERIA G2 1 TB、TOPMORE Gemini 2 TB NVMe SSD。
- Mi Monitor，3840 × 2160，约 160 Hz。
- Arch Linux，niri 26.04 / Wayland。

后台可直接照着填写。若通过资源 API 导入示例，先创建分组，再将 `groupName` 换成该分组的实际 `metadata.name`；显示名称不等于资源名称。

`build/reports/equipment-preview.html` 是测试用实际 Thymeleaf 模板生成的独立预览，可本地打开；其中手机和笔记本是明确标注的填写示例，不代表已识别到你的设备。
