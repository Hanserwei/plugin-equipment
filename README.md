<p align="center">
  <img src="src/main/resources/logo.svg" width="88" height="88" alt="han-equipment">
</p>

# han-equipment

[![CI](https://github.com/Hanserwei/plugin-equipment/actions/workflows/ci.yaml/badge.svg)](https://github.com/Hanserwei/plugin-equipment/actions/workflows/ci.yaml)
[![CD](https://github.com/Hanserwei/plugin-equipment/actions/workflows/cd.yaml/badge.svg)](https://github.com/Hanserwei/plugin-equipment/actions/workflows/cd.yaml)
[![Halo](https://img.shields.io/badge/Halo-%E2%89%A5%202.26.0-blue)](https://www.halo.run/)
[![License](https://img.shields.io/badge/license-GPL--3.0-green)](LICENSE)

为 Halo 博客展示你的电脑、手机、外设与操作系统。在 Console 中填写设备信息，用有序参数表展示配置，用卡片记录日常使用的装备。

由 [Hanserwei](https://github.com/Hanserwei) 独立维护，基于困困鱼的 [plugin-equipment](https://github.com/chengzhongxue/plugin-equipment)。插件标识为 **`han-equipment`**，使用独立的数据和权限命名空间。

[下载版本](https://github.com/Hanserwei/plugin-equipment/releases) · [主题适配](docs/theme-integration.md) · [反馈问题](https://github.com/Hanserwei/plugin-equipment/issues) · [发布流程](docs/releasing.md)

**2.0.0 是 han-equipment 由 Hanserwei 独立维护后发布的首个版本。** 本版提供装备分类、结构化参数、使用状态、重点展示和主题卡片集成，完整说明见 [2.0.0 发布说明](docs/releases/2.0.0.md)。

## 能做什么

| 功能 | 说明 |
| --- | --- |
| 设备分类 | 台式机、笔记本、手机、平板、外设、操作系统与其他装备 |
| 配置参数 | 自定义名称和内容，支持增删、排序、重复名称与多行文本 |
| 参数模板 | 一键补充当前设备类型的常用字段，保留已经填写的参数 |
| 使用状态 | 使用中、备用、已退役 |
| 重点展示 | 为主力设备使用宽卡片，容纳更多配置 |
| 图片与链接 | 图片可选，无图时展示类型图标；可链接产品官网或自己的文章 |
| 分组管理 | 按工作、移动设备、外设等用途分组，支持排序与批量操作 |
| 主题集成 | 内置响应式页面和可复用卡片，支持深色模式样式 |

插件用于**手动维护的公开装备清单**，不提供硬件实时监控、自动设备扫描或 fastfetch 一键导入。

## 安装

运行插件需要 **Halo >= 2.26.0**。普通使用者不需要安装 Node.js、pnpm 或 Gradle。

1. 从 [GitHub Releases](https://github.com/Hanserwei/plugin-equipment/releases) 下载 `han-equipment-<版本>.jar`，不要使用 GitHub 自动生成的 Source code 压缩包。若尚无 Release，可以按下文从源码构建。
2. 在 Halo Console 的“插件”页面选择本地安装，上传 JAR。
3. 启用 **han-equipment**，进入左侧的 **Han 装备** 菜单。
4. 创建分组，添加装备并填写参数。
5. 访问 `https://你的域名/equipments` 查看展示页面。

如已安装旧 `equipment` 插件，请先停用它。两个插件的身份和数据空间不同，但都使用 `/equipments` 前台路由，不应同时启用。

## 填写第一台设备

以一台台式电脑为例：

1. 新建“日常设备”分组，点击“新增”。
2. 填写名称，选择“台式电脑”，状态设为“使用中”；需要宽卡片时勾选“重点展示”。
3. 点击“添加类型预设参数”，填写处理器、显卡、内存、硬盘和操作系统等信息。
4. 删除不需要的字段，添加自定义参数，使用上下箭头调整展示顺序。
5. 可选填写图片、简短概述、使用感受与相关链接，保存。

| 参数名称 | 内容示例 |
| --- | --- |
| 处理器 | Intel Core i9-14900K / 24 核心、32 线程 |
| 显卡 | NVIDIA GeForce RTX 3060 / 12 GB |
| 操作系统 | Arch Linux / Rolling Release |
| 窗口管理器 | niri / Wayland |

参数完全空白的行会在保存时忽略；只填名称或只填内容的行会提示补全。配置值可以换行，适合多块硬盘、多台显示器或多系统。

[desktop.json](docs/examples/desktop.json) 提供一份根据 fastfetch 输出整理的资源示例。后台不直接导入此文件；通过资源 API 导入时，需要将 `groupName` 改为实际分组的资源名称。

## 主题适配

Halo 优先使用主题自己的 `equipments.html`。主题没有该模板时，会使用插件内置页面；已有模板需要读取新字段或引入插件卡片。

Hanlo 的推荐接入方式是在 `src/equipments.html` 中修改两处：

```html
<!-- 在 head 片段中引入样式 -->
<link rel="stylesheet"
      th:href="@{/plugins/han-equipment/assets/static/equipment.css(v='2.0.0')}">

<!-- 用这个片段替换旧装备列表，保留主题导航、横幅与页脚 -->
<th:block th:replace="~{plugin:han-equipment:modules/equipment :: list(groups=${groups})}"></th:block>
```

完整位置、数据字段、单张卡片复用方式和深色模式说明见 [主题适配指南](docs/theme-integration.md)。插件不会自动修改主题源码。

## 升级、禁用与卸载

- **升级 han-equipment**：先通过 Halo 备份功能备份站点，再安装新版本 JAR。确认 Release 说明中的 Halo 最低版本和数据变更要求。
- **从旧 equipment 切换**：这是独立插件，原 API 分组 `equipment.kunkunyu.com` 的数据不会自动迁移到新分组。已有数据请先备份，再按新字段重新录入；已写入主题的旧插件资源路径也要调整。
- **禁用**：在插件管理中禁用后，管理入口、Finder 和插件路由不可用。使用插件模板片段的自定义页面也需要同步处理。
- **卸载**：先备份装备数据，并移除菜单中的 `/equipments` 链接及主题对插件片段/Finder 的引用，再从 Halo 插件管理卸载。不要把卸载操作当作备份或迁移方式。

## 数据与外部服务

装备名称、图片、配置、分组和描述保存在你的 Halo 中，并通过展示页和公开查询 API 提供给访客。请按公开页面的用途填写内容。

插件不主动读取你的硬件、不上传设备信息到第三方，也不要求外部账号或付费服务。若使用外部图片地址，访客浏览器会请求对应图片服务；点击相关链接会打开其目标网站。

## 常见问题

| 问题 | 检查方式 |
| --- | --- |
| `/equipments` 返回 404 | 检查 Halo 版本、插件是否启用，以及服务器日志中是否有启动错误 |
| 保存了参数，但前台没显示 | 检查主题是否覆盖 `equipments.html`，按适配指南引入新片段 |
| 页面报模板或资源找不到 | 确认路径使用 `plugin:han-equipment:` 和 `/plugins/han-equipment/`，并确保插件已启用 |
| 找不到“Han 装备”菜单 | 检查账号是否有“Han 装备查看/管理”权限，安装后刷新 Console |
| 旧插件的数据没有出现 | 新旧 API 分组独立，本插件没有自动迁移旧数据 |
| 删除分组后装备也被删除 | 删除分组会一并删除组内装备；需要保留时，先将装备移动到其他分组 |

反馈问题时请提供 Halo 版本、插件版本、主题名称、复现步骤及相关错误日志；提交前移除令牌等私密信息。[创建 Issue](https://github.com/Hanserwei/plugin-equipment/issues/new)。

## 从源码构建

| 工具 | 版本 |
| --- | --- |
| JDK | 21，`JAVA_HOME` 指向该 JDK |
| Node.js | >= 24.15.0，推荐 Node 24 LTS |
| pnpm | 11.25.0，由 Gradle / Corepack 按项目配置使用 |
| Python | 3.9+，用于 Release 校验脚本测试 |
| Gradle | Wrapper 9.7.1，无需全局安装 |

```bash
git clone https://github.com/Hanserwei/plugin-equipment.git
cd plugin-equipment
./gradlew clean build
```

Windows 可使用 `gradlew.bat clean build`；Release 脚本测试需要命令行能执行 `python3`。

构建产物：

```text
build/libs/han-equipment-2.0.0.jar
build/libs/han-equipment-2.0.0.jar.sha256
```

完整构建包含 Java 测试、Vue 类型检查、UI 测试、lint、Release 校验脚本测试及 JAR 内容检查。校验和与 JAR 一同分发，可以在文件所在目录运行 `sha256sum -c han-equipment-2.0.0.jar.sha256`。

常用开发命令：

```bash
./gradlew test                   # Java / Thymeleaf 测试
./gradlew :ui:pnpmCheck          # UI 测试
./gradlew :ui:pnpmLint           # UI lint
./gradlew verifyPluginArchive    # 检查 JAR 并生成校验和

cd ui
corepack pnpm install --frozen-lockfile
corepack pnpm type-check
corepack pnpm dev                # 监听 Console UI 变更并构建
```

`./gradlew test` 会生成 `build/reports/equipment-preview.html`，使用实际 Thymeleaf 模板生成静态预览；其中手机和笔记本为示例。自动化测试不等同于真实 Halo 中的安装、启停和升级验收。

前后端基于 Halo API/UI 2.26.0、Vue 3.5、Rsbuild 2、TypeScript 6、Vitest 5。Vue Query 4 与 Vue Router 5.1 跟随 Halo Console 的共享依赖版本。依赖由 `ui/pnpm-lock.yaml` 固定，配置文件列出具体版本。

## CI/CD 与发布

CI 使用 Halo 官方 reusable workflow v4；CD 复用其构建环境，独立支持自动发布和手动构建。引用的工作流与 action 固定到已核对的提交。

| 工作流 | 触发条件 | 执行内容 |
| --- | --- | --- |
| CI | 推送到 `main`、面向 `main` 的 PR、手动运行 | `clean build`，包括前后端检查及 JAR 校验；PR 构建附带临时产物 |
| CD | 发布 GitHub Release，或在 Actions 中手动指定版本标签 | 校验标签，执行完整 `clean build`，验证 JAR 和校验和，保存 14 天构建产物；自动发布或手动勾选上传时，将附件上传到已有 Release |

Release 标签可使用 `v2.0.0` 或 `2.0.0`，也支持 `v2.1.0-rc.1`。标签版本必须与该提交中的 `gradle.properties`、`plugin.yaml` 一致。CD 的构建、测试或校验失败时不会上传 Release 产物；同一版本发布后不要替换制品。

手动运行 CD 默认只构建，在 **Actions → CD → Run workflow** 中填写 `tag`；需要向已发布的 GitHub Release 补传附件时，再勾选 `publish`。构建始终使用该标签的源码，不会使用手动运行时所选分支的业务代码。上传前会再次核对标签指向，且不会覆盖已有同名附件。

目前只配置 **GitHub Release 附件发布**，工作流未接入应用市场，不需要 App ID 或 `HALO_PAT`。首次上架和审核后的自动同步配置按需另行处理。

维护者操作步骤见 [发布指南](docs/releasing.md)，Halo 官方要求见 [发布应用](https://docs.halo.run/developer-guide/app-store/publish-app.md)。

## 开发者接口

| 标识 | 值 |
| --- | --- |
| 插件 `metadata.name` | `han-equipment` |
| Console 页面 | `/console/han-equipments` |
| 前台页面 | `/equipments` |
| 资源 API 分组 | `equipment.hanserwei.github.io/v1alpha1` |
| Console API 分组 | `console.api.equipment.hanserwei.github.io/v1alpha1` |
| 公开查询 API 分组 | `api.equipment.hanserwei.github.io/v1alpha1` |
| 主题 Finder | `hanEquipmentFinder` |
| 静态资源前缀 | `/plugins/han-equipment/assets/` |

资源类型为 `Equipment` 和 `EquipmentGroup`。`attributes` 是 `{ label, value }[]`，顺序即展示顺序；详见 [字段契约](docs/theme-integration.md#新增字段与模板契约)。

## 来源与许可证

本项目基于困困鱼的 [chengzhongxue/plugin-equipment](https://github.com/chengzhongxue/plugin-equipment)，保留上游历史与原有许可证，并由 Hanserwei 独立维护。当前插件 Logo 为本项目绘制，Console 中的 Tabler 图标来自 [Tabler Icons](https://github.com/tabler/tabler-icons)（MIT）。

项目使用 [GPL-3.0](LICENSE)。问题反馈和后续开发在[本仓库](https://github.com/Hanserwei/plugin-equipment)进行。
