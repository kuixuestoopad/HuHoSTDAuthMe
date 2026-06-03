package com.stoopad.authme.manager;

import com.stoopad.authme.HuHoSTDAuthMe;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.security.SecureRandom;

/**
 * AuthMe 密码管理器
 * 通过反射直接调用 AuthMe 内部方法设置密码
 */
public class AuthMeManager {

    private final HuHoSTDAuthMe plugin;
    private final SecureRandom random = new SecureRandom();
    private Object authMeApi;
    private Method apiSetPassword3; // setPassword(player, pass, hash)
    private Object passwordSecurity; // PasswordSecurity 实例

    private static final String PASSWORD_CHARS = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";

    public AuthMeManager(HuHoSTDAuthMe plugin) {
        this.plugin = plugin;
        initApi();
    }

    private void initApi() {
        // 方案1: AuthMe API (v3)
        try {
            Class<?> apiClass = Class.forName("fr.xephi.authme.api.v3.AuthMeApi");
            Method getInstance = apiClass.getMethod("getInstance");
            authMeApi = getInstance.invoke(null);
            apiSetPassword3 = apiClass.getMethod("setPassword", String.class, String.class, boolean.class);
            plugin.getLogger().info("已连接 AuthMe API v3 (setPassword 3参数)");
            return;
        } catch (Exception ignored) {}

        // 方案2: AuthMe API (legacy)
        try {
            Class<?> apiClass = Class.forName("fr.xephi.authme.api.AuthMeApi");
            Method getInstance = apiClass.getMethod("getInstance");
            authMeApi = getInstance.invoke(null);
            apiSetPassword3 = apiClass.getMethod("setPassword", String.class, String.class, boolean.class);
            plugin.getLogger().info("已连接 AuthMe API legacy (setPassword 3参数)");
            return;
        } catch (Exception ignored) {}

        // 方案3: 直接获取 PasswordSecurity 实例
        try {
            Class<?> injectorClass = Class.forName("fr.xephi.authme.AuthMe");
            plugin.getLogger().info("AuthMe 主类: " + injectorClass.getName());

            // 尝试通过 Bukkit.getPluginManager 获取 AuthMe 实例
            org.bukkit.plugin.Plugin authMePlugin = Bukkit.getPluginManager().getPlugin("AuthMe");
            if (authMePlugin != null) {
                // 尝试获取 PasswordSecurity 字段
                for (java.lang.reflect.Field field : authMePlugin.getClass().getDeclaredFields()) {
                    if (field.getType().getSimpleName().equals("PasswordSecurity")) {
                        field.setAccessible(true);
                        passwordSecurity = field.get(authMePlugin);
                        plugin.getLogger().info("找到 PasswordSecurity: " + field.getName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("PasswordSecurity 获取失败: " + e.getMessage());
        }

        plugin.getLogger().warning("AuthMe API 未就绪，将使用命令方式");
    }

    /**
     * 为玩家重置密码
     * @return 新密码，失败返回 null
     */
    public String resetPassword(String playerName) {
        String newPassword = generatePassword(8);
        plugin.getLogger().info("[重置密码] 玩家=" + playerName + " 新密码=" + newPassword);

        // 方案1: API setPassword(player, pass, true)
        if (apiSetPassword3 != null && authMeApi != null) {
            try {
                boolean success = (boolean) apiSetPassword3.invoke(authMeApi, playerName, newPassword, true);
                plugin.getLogger().info("[重置密码] API setPassword(3): " + success);
                if (success) return newPassword;
            } catch (Exception e) {
                plugin.getLogger().warning("[重置密码] API 异常: " + e.getMessage());
            }
        }

        // 方案2: 反射调用 PasswordSecurity.setPassword(player, pass)
        if (passwordSecurity != null) {
            try {
                Method setPw = passwordSecurity.getClass().getMethod("setPassword", String.class, String.class);
                setPw.invoke(passwordSecurity, playerName, newPassword);
                plugin.getLogger().info("[重置密码] PasswordSecurity.setPassword 已调用");
                return newPassword;
            } catch (Exception e) {
                plugin.getLogger().warning("[重置密码] PasswordSecurity 异常: " + e.getMessage());
            }
        }

        // 方案3: 命令方式（主线程）
        plugin.getLogger().info("[重置密码] 尝试命令方式");
        try {
            // AuthMe 命令是 authme password，不是 setpassword
            boolean success = Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "authme password " + playerName + " " + newPassword
            );
            plugin.getLogger().info("[重置密码] authme password 命令返回: " + success);
            if (success) return newPassword;
        } catch (Exception e) {
            plugin.getLogger().severe("[重置密码] 命令异常: " + e.getMessage());
        }

        plugin.getLogger().severe("[重置密码] 全部失败！");
        return null;
    }

    public boolean isApiReady() {
        return authMeApi != null || passwordSecurity != null;
    }

    private String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
