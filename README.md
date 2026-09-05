# 装备管理

由 [Hanserwei](https://github.com/Hanserwei) 独立维护的 Halo 装备展示插件，仓库地址为 [Hanserwei/plugin-equipment](https://github.com/Hanserwei/plugin-equipment)。基于 [chengzhongxue/plugin-equipment](https://github.com/chengzhongxue/plugin-equipment)，上游作者为困困鱼，项目沿用 GPL-3.0 许可证。

在 Halo Console 中填写台式机、笔记本、手机、平板、外设和操作系统，前台通过 `/equipments` 展示。

## 功能

- 设备类型、使用状态与重点展示开关。
- 可增删、排序的配置参数表，支持多行内容、重复参数名和自定义字段。
- 台式机、笔记本、手机、平板、外设、操作系统的常用参数模板；添加模板时保留已填写内容。
- 设备图片可选，无图片时使用类型图标。
- 响应式卡片、重点设备宽卡片、完整参数与深色模式样式。
- 分组管理、拖拽排序、可选详情链接。

## 安装与主题适配

**2.0.0 需要 Halo >= 2.26.0。** 通过 Console 安装构建生成的 `build/libs/plugin-equipment-2.0.0.jar`，启用后创建分组和装备。

主题没有 `equipments.html` 时，会使用插件自带页面。如果主题已有该模板，需接入新卡片或自行读取新增字段。

**Hanlo 只需两处修改：引入插件 CSS，替换旧卡片循环。完整可复制的代码见 [主题适配指南](docs/theme-integration.md)。** 本项目不直接修改主题源码。

[本机台式电脑配置示例](docs/examples/desktop.json) 来自 fastfetch 与显存查询，供手动填写或自行导入使用；插件不会自动读取、上传或发布本机配置。

## 开发环境

| 项目 | 版本 |
| --- | --- |
| Halo API / BOM / UI 组件 | 2.26.0 |
| Java | 21 |
| Gradle Wrapper | 9.7.1 |
| Halo DevTools | 0.8.0 |
| Lombok Gradle 插件 | 9.5.0 |
| Node.js | >= 24.15.0（推荐 Node 24 LTS） |
| pnpm | 11.25.0 |
| Vue | 3.5.42 |
| Rsbuild | 2.2.3 |
| TypeScript / vue-tsc | 6.0.3 / 3.3.11 |
| Vitest | 5.0.0 |

Vue Query 使用 4.44.x、Vue Router 使用 5.1.x，与 Halo 2.26 Console 的共享依赖一致。TypeScript 6 与当前 Halo 工具链对齐。Halo UI 依赖使用正式 npm 包，不再引用 `pkg.pr.new` 临时预览包。

```bash
# 完整编译、UI 类型检查、前后端测试与打包
./gradlew build

# 单独运行前端检查
cd ui
corepack pnpm install --frozen-lockfile
corepack pnpm type-check
corepack pnpm test:unit
corepack pnpm lint
```

Gradle 固定使用 pnpm 11.25.0；`ui/pnpm-lock.yaml` 纳入版本控制。pnpm 构建脚本仅允许 Vue Query 依赖的 Vue Demi 生成本地 Vue 兼容导出。

`ui/pnpmCheck` 使用 `vitest run`，执行后退出。Gradle 9 的 JUnit Platform Launcher 已显式声明。

## 验证与预览

- UI 测试覆盖参数模板、空行校验、实际 FormKit 创建/编辑、排序删除、分组和元数据保留、设备切换。
- Java 测试直接渲染 Thymeleaf 片段，覆盖完整配置、无图片、空分组、链接处理和文本转义。
- 运行 `./gradlew test` 后会生成 `build/reports/equipment-preview.html`。此预览由实际模板生成；手机和笔记本内容为标注过的示例。

测试只在本项目内运行，不要求安装到真实 Halo，不向任何站点写入装备数据。

## 独立维护说明

此版本移除了上游应用商店 App ID，Release 工作流只上传 GitHub Release 附件，不向原作者的应用商店条目发布。插件资源名称 `equipment` 保持稳定，便于在自己的 Halo 中替换上游安装。

本地 `origin` 指向 `Hanserwei/plugin-equipment`，`upstream` 保留为 `chengzhongxue/plugin-equipment`，用于查阅上游变更。日常提交推送到自己的 `main` 分支，独立维护，无需向上游发起 PR。问题反馈请提交到[本仓库 Issues](https://github.com/Hanserwei/plugin-equipment/issues)。
