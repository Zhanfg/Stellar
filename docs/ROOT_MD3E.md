# Stellar · Root / Material 3 Expressive 改造（开发候选）

目标：兼容 Android 14–17，优先 Android 17（API 37），改善 KernelSU / APatch / Magisk 的 Root 启动和管理器 MD3E 体验。

## Root 可靠性

- **Root 申请**：不再以 `PATH` 中是否存在 `su` 决定按钮是否可用；由 libsu 在显式点击 Root 启动时实际申请和验证权限。
- **开机启动**：`BootScriptManager` 原来使用 `ls $(pm path ...)` 拼接路径，更新应用后易失效；现在 `service.d` 脚本每次开机重新解析 `base.apk` 与匹配 ABI 的 `libstellar.so`。
- **原子写入**：安装 `service.d` 脚本使用临时文件 + `chmod 0700` + `mv`，异常不会直接截断旧的已安装脚本。
- **有限重试**：仅等待系统开机完成和软件包可用，并设置重试上限，不增加常驻轮询进程。
- **进程保护**：开机脚本发现 `stellar_server` 已在运行时跳过重复启动。
- **原生 starter**：先校验 APK 的存在和访问权限，再处理旧服务；修复 `dirname()` 修改 `std::string::c_str()` 内存的未定义行为。
- 不自动授予第三方应用 Root；不绕过 SELinux；不修改 su 管理器策略。

## MD3E 全局基础

- 使用 Compose Material 3 官方 `ShortNavigationBar` / `ShortNavigationBarItem` 和高辨识度的选中指示器。
- 宽度达到 600dp 时切换侧栏，不再仅根据横竖屏判断。
- 移除对 `LocalDensity` 与 `fontScale` 的全局缩放，遵循用户显示缩放和辅助功能文字比例。
- 导航切换遵循 back stack，保留各 Tab 状态；返回时更新选中项。
- `MaterialTheme` 提供统一的圆角与标题字体权重，项目专属卡片、输入框和对话框与主题形状对齐。
- 保留 Android 12+ 系统动态配色，支持深色模式，并设置状态栏与手势栏的 Edge-to-Edge 透明绘制。
- 当前使用稳定版兼容的 Material 3 组件；没有直接将发布依赖强行升级至未验证的 `1.5.x` alpha。

## 验收项

1. Android 17 / KernelSU、APatch、Magisk：分别测试安装后授权、拒绝 Root、再次申请、降权开关。
2. 验证 `service.d` 的安装、卸载、重启拉起、应用更新后路径变化、服务已存在时不重复启动。
3. 验证应用路径错误时旧服务保持工作；Root 转换、Binder 注册、权限管理没有行为回归。
4. 横竖屏、420 DPI、窄屏和大字体：无遮挡、导航不错位，键盘弹出后输入框仍可用。
5. Android 12–17：壁纸动态色、深色/浅色切换、手势导航、系统返回、状态栏与对话框。
6. CI :manager:assembleDebug 与 :manager:testDebugUnitTest 全通过，然后按真机结果决定是否合并。

## 回退策略

- 开发更改全部位于 PR #1，主线 `main` 未变动。
- Root 开机启动在应用设置中启用/关闭；卸载 `service.d/stellar.sh` 后恢复原启动方式。
- 只有真机验证通过后才能将 Draft PR 改为正式合并候选。

**状态：实现已提交，未完成真机兼容性和视觉验收，不可当作稳定版宣称。**
