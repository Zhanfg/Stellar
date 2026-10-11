# Stellar Android 17 兼容与性能修复

开发分支：`feat/android17-stability-performance`（不直接覆盖 main）

## 基线

- Android 17：SDK 37（仓库现有 compileSdk/targetSdk 均为 37）。
- Android 17 局域网保护：使用 `ACCESS_LOCAL_NETWORK` 运行时权限；未授权前不启动 NSD。
- 不改变包名、Binder/AIDL 协议、签名或已有授权配置。
- 不依赖额外常驻进程、Root 模块或 LSPosed Hook。

## 本次修改

1. `AdbMdns`：将重启式服务发现的检查周期从 500 ms 增至 5 s；限制重试次数；消除频繁停止/重启 NSD 的电量开销。
2. `AdbMdns`：不再将「端口无法 bind」误判为「ADB 可连接」，改为对本机 mDNS 所公布的地址和端口执行短超时 TCP 连接检测。
3. `AdbMdns`：增加发现会话版本戳，防止 stop/start 之后旧异步结果再次触发回调；服务丢失时恢复发现，并在后台执行网络检查。
4. `ClientManager`：同步维护 `uid/pid` 与 `uid` 双索引；仅清除匹配的旧 Binder 记录，避免并发死亡回调删除新连接。
5. `ConfigManager`：只有配置实际变化才安排持久化，序列化在锁内做快照，Binder I/O 不持锁；首次读取管理器配置失败时不以默认数据覆盖原配置。
6. `PermissionManagerCompat`：对 SDK 37 `IPermissionManager` 新增的 `deviceId` 参数采用缓存反射兜底；不静默吞掉权限授予/撤销失败。
7. `ManagerGrantHelper`：管理器授权检查使用 SDK 37 兼容封装。

## 验收矩阵（需要 CI + 真机验证）

| 场景 | 预期 |
|---|---|
| Android 17 / SDK 37：局域网权限未授权 | 显示权限请求，不开始无效 NSD 扫描 |
| Android 17 / SDK 37：授权无线调试 | 完成 mDNS 发现、配对、ADB 启动、Binder 交付 |
| Android 17：已授权应用列表 | 能列出所有对应用户的应用，无 `NoSuchMethodError` |
| Android 17：运行时权限授予/撤销 | 真正改变权限，不报告虚假成功 |
| Android 14–16：Root 与 ADB 启动 | 无行为回归 |
| 两个线程连接同一个 `uid/pid` | 不产生重复记录或误删新实例 |
| 管理器重启、服务丢失/恢复 | 能重新发现，旧回调不污染新会话 |
| 首次配置读取失败 | 不覆盖原有持久化授权 |
| 30 分钟空闲观察 | 不出现密集 mDNS 重启或持续高 CPU 唤醒 |

**注意**：源码修改不等于设备验证。CI 成功后仍应分别完成 Android 17 Root、无线 ADB、权限、重启、休眠测试。此阶段仅作为开发候选，不应自动合并主线或标记稳定正式版。

参考：
- https://developer.android.com/privacy-and-security/local-network-permission
- https://developer.android.com/reference/android/net/nsd/NsdManager
- https://github.com/kerneldroid/Nightzuku/blob/main/docs/android-17-compatibility.md
