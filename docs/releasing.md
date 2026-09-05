# han-equipment 发布指南

当前流水线只负责 GitHub 上的构建和 Release 附件，不向 Halo 应用商店同步。本指南参考 [Halo 发布应用](https://docs.halo.run/developer-guide/app-store/publish-app.md) 和[插件发布验收清单](https://docs.halo.run/developer-guide/plugin/release-checklist.md)。

## 版本来源

这三个值必须一致：

```text
gradle.properties                  version=2.0.0
src/main/resources/plugin.yaml     spec.version: 2.0.0
GitHub Release tag                 v2.0.0 或 2.0.0
```

支持 `2.1.0-rc.1` 等 SemVer 预发布版本。发布预览版本时，在 GitHub Release 中标记 Pre-release。

不兼容变更增加主版本号；兼容的新功能增加次版本号；修复增加补丁号。更新 `plugin.yaml` 的 `spec.requires` 时，要确认源码使用的 Halo API 与最低版本一致。

插件标识固定为 `han-equipment`，不能随版本变动。它与上游 `equipment` 是两个插件，使用各自的数据和权限资源。

## 发布前准备

1. 修改版本，补充功能、修复和兼容性变更说明。需要更新主题 CSS 缓存版本时同步修改适配示例。
2. 执行本地验证：

   ```bash
   ./gradlew clean build
   RELEASE_TAG=v2.0.0 python3 scripts/check-release.py
   ```

3. 提交并推送到 `main`，确认 GitHub CI 通过。
4. 下载或使用最终构建的 JAR，按 Halo 官方清单另行验证安装、启用、配置、页面访问、禁用及卸载。自动化单元测试不代表这些流程已经验收。
5. 准备准确的 README、版本说明、Logo、实际运行截图和反馈地址。仓库的静态模板预览用于开发，不应被描述成实机截图。

上述本地命令不会安装插件到 Halo、创建 GitHub Release 或提交应用市场审核；实机验收需要另行进行。

## 在 GitHub 发布

1. 打开仓库的 **Releases → Draft a new release**。
2. 选择已经验证的提交或标签，标签使用与源码一致的版本，例如 `v2.0.0`。
3. 填写版本说明，说明新增功能、最低 Halo 版本和主题适配变化。
4. 发布 Release 后，CD 自动开始运行。
5. 在 Actions 中确认 CD 成功；Release 附件应包含：

   ```text
   han-equipment-2.0.0.jar
   han-equipment-2.0.0.jar.sha256
   ```

`Source code (zip/tar.gz)` 是 GitHub 自动提供的源码归档，不是可安装插件。用户应下载 JAR。

2.0.0 是独立维护的首个版本，发布说明保存在 [`releases/2.0.0.md`](releases/2.0.0.md)。在标签已推送、Release 尚未创建时，也可以从仓库根目录用 GitHub CLI 发布：

```bash
gh release create 2.0.0 --verify-tag \
  --title "han-equipment 2.0.0 · 独立维护首个版本" \
  --notes-file docs/releases/2.0.0.md
```

创建 Release 时不要手动附加本地 JAR，等待 CD 上传经过检查的构建产物。

## CI 做什么

`.github/workflows/ci.yaml` 在以下情况运行：

- `main` 收到推送。
- 有面向 `main` 的 Pull Request。
- 维护者从 Actions 页面手动运行。

CI 使用固定到提交的 Halo reusable workflows v4，环境为 JDK 21、Node 24、pnpm 11.25.0，执行 `./gradlew clean build`。构建包含：

- Java / Thymeleaf 测试。
- Vue 类型检查、Vitest 测试与 UI lint。
- Release 标签校验脚本的测试。
- 插件 JAR 内容检查和 SHA-256 生成。

同一分支或 PR 的新 CI 会取消仍在运行的旧 CI。官方工作流只为 PR 上传临时构建产物，保留 1 天；稳定下载使用 GitHub Releases。

## CD 做什么

`.github/workflows/cd.yaml` 支持 **GitHub Release published** 和 **workflow_dispatch**。推送普通提交或只推送标签不会发布附件。

1. 根据 Release 的标签或手动输入的 `tag` 检出 `refs/tags/<tag>`，校验合法 SemVer，并确认与该提交中的两个版本声明一致。
2. 使用 Halo 官方构建环境（JDK 21、Node 24、pnpm 11.25.0），执行 `./gradlew clean build --no-daemon --console=plain`，包含 CI 中的全部检查和测试。
3. `build` 依赖 `verifyPluginArchive`：验证 JAR 文件名、内部插件标识与版本、模板、样式、Logo、扩展资源，以及 Console manifest 对应的实际入口文件。
4. 验证 SHA-256，将 JAR 和校验和保存为 `han-equipment-<version>-<commit>` 构建产物，保留 14 天。
5. 自动发布，或手动勾选 `publish` 时，独立的上传任务下载本次构建产物，再次检查 SHA-256、标签指向和 Release 已发布状态，最后上传 JAR 与校验和。

构建任务只有读取仓库的权限，只有上传任务拥有 `contents: write`。同一标签的 CD 串行执行，不取消已经开始的发布；标签在构建后被改动时，上传会失败。上传不使用 `--clobber`，同名附件已经存在时会报错。

CI 继续使用 Halo 官方 reusable workflow；CD 复用其固定到提交的 `plugin-setup-env`，以支持手动入口并直接执行完整构建。工作流未声明 `HALO_PAT` 或 App Store App ID。

## 手动构建和补传

在 **Actions → CD → Run workflow** 中保留工作流分支为 `main`，填写已经存在的版本标签。业务源码始终取自这个标签。

- 默认 `publish: false`：只做完整构建，在 Actions 中提供 JAR 和校验和，不需要对应的 Release 已创建。
- 勾选 `publish`：完整构建通过后，将附件补传到该标签对应的已发布 Release。不会创建 Release，也不会覆盖已有同名附件。

CLI 示例：

```bash
# 验证指定版本，只生成 Actions 构建产物
gh workflow run cd.yaml --ref main -f tag=2.0.0 -F publish=false

# 对已有 Release 补传构建产物
gh workflow run cd.yaml --ref main -f tag=2.0.0 -F publish=true
```

如果 Release 已发布但没有 CD 运行记录，可通过手动入口补传，无需重复删除、创建 Release。

## 失败与重试

- **标签与版本不一致**：修正版本声明或发布所选提交；不要通过跳过校验发布名称不一致的 JAR。
- **pnpm 锁文件不一致**：在 `ui/` 中用指定的 pnpm 版本重新安装并提交 `pnpm-lock.yaml`，不要在 CI 里取消 frozen lockfile。
- **JAR 缺少文件或内部版本错误**：查看 `verifyPluginArchive` 输出，修复打包配置后重新运行本地干净构建。
- **GitHub Actions 未启用**：fork 默认状态可能与原仓库不同，在 Actions 页面确认工作流已启用。
- **Release 没有触发 CD**：确认标签提交包含工作流且 Actions 已启用；GitHub Actions 使用默认 `GITHUB_TOKEN` 创建 Release 时，不会再触发其他工作流。可从上述手动入口运行，并查看是否有审批或执行限制。
- **上传失败**：检查 GitHub Release 和 Actions 日志，确认 Release 已发布、标签未移动、同名附件不存在。下载已有附件后可先校验；已正式分发的版本有问题时，应修复后发布新版本。

下载校验：

```bash
sha256sum -c han-equipment-2.0.0.jar.sha256
```

## 暂不配置的事项

应用市场开发者入驻、创建应用、首次审核、App ID、个人令牌以及审核后自动同步均未配置。未来需要上架时，先准备并审核当前应用自己的资料，再按官方文档增加设置；不得复用上游插件的应用商店身份。
