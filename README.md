# HuHoSTDAuthMe

HuHoBot 扩展插件 — QQ 群内强制登陆 & 重置密码（AuthMe）

## 功能

| QQ 群命令 | 效果 |
|---|---|
| `@机器人 强制登陆` / `@机器人 强制登录` | 绑定的 MC 账号在线时，调用 `authme forcelogin` 跳过登录验证 |
| `@机器人 重置密码` | 在线时生成随机密码，通过 `authme setpassword` 设置，踢出并显示新密码 |

## 前置依赖

| 插件 | 说明 | 下载 |
|---|---|---|
| **HuHoBot** | QQ 机器人框架（Spigot 端） | [GitHub](https://github.com/Huohuas001/HuHoBot) |
| **HuHoSTDWhiteList** | QQ 验证码绑定白名单 | [GitHub](https://github.com/kuixuestoopad/HuHoSTDWhiteList) |
| **AuthMe** | MC 登录验证插件 | [GitHub](https://github.com/AuthMe/AuthMeReloaded) |

- 服务器版本：Paper 1.21+
- Java：21

## 安装

1. 确保以上三个前置插件已安装并正常运行
2. 下载 Release 中的 `HuHoSTDAuthMe.jar` 放入 `plugins/` 目录
3. 重启服务器

## 构建

```bash
# 先把前置 jar 放入 libs/ 目录
mkdir -p libs
# HuHoBot-Spigot.jar 从 HuHoBot 项目获取
# HuHoSTDWhiteList.jar 从 HuHoSTDWhiteList 项目构建

gradle jar
```

产物在 `build/libs/HuHoSTDAuthMe.jar`

## 工作原理

- **强制登陆**：通过 HuHoBot 的 `BotCustomCommand` 事件接收 QQ 群消息，查找 BindManager 中的绑定关系，对在线玩家执行 `authme forcelogin`
- **重置密码**：生成 8 位随机密码，通过 `authme setpassword` 控制台命令设置（AuthMe 自动处理哈希，兼容 MySQL/SQLite/任意哈希算法），然后踢出玩家并在踢出消息中显示新密码

## License

MIT
