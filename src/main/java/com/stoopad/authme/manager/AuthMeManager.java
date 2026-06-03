package com.stoopad.authme.manager;

import com.stoopad.authme.HuHoSTDAuthMe;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.security.SecureRandom;

/**
 * AuthMe 密码管理器
 * 通过 AuthMe Java API 直接设置密码，不依赖命令
 */
public class AuthMeManager {

    private final HuHoSTDAuthMe plugin;
    private final SecureRandom random = new SecureRandom();
    private Object authMeApi; // AuthMeApi 实例

    private static final String PASSWORD_CHARS = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";

    public AuthMeManager(HuHoSTDAuthMe plugin) {
        this.plugin = plugin;
        initApi();
    }

    /**
     * 通过反射获取 AuthMeApi 实例
     */
    private void initApi() {
        try {
            // AuthMe 5.x: fr.xephi.authme.api.v3.AuthMeApi.getInstance()
            Class<?> apiClass = Class.forName("fr.xephi.authme.api.v3.AuthMeApi");
            Method getInstance = apiClass.getMethod("getInstance");
            authMeApi = getInstance.invoke(null);
            plugin.getLogger().info("已连接 AuthMe API (v3)");
        } catch (ClassNotFoundException e) {
            try {
                // 旧版: fr.xephi.authme.api.AuthMeApi.getInstance()
                Class<?> apiClass = Class.forName("fr.xephi.authme.api.AuthMeApi");
                Method getInstance = apiClass.getMethod("getInstance");
                authMeApi = getInstance.invoke(null);
                plugin.getLogger().info("已连接 AuthMe API (legacy)");
            } catch (Exception e2) {
                plugin.getLogger().severe("AuthMe API 加载失败: " + e2.getMessage());
            }
        } catch (Exception e) {
            plugin.getLogger().severe("AuthMe API 初始化失败: " + e.getMessage());
        }
    }

    /**
     * 为玩家重置密码
     * @return 新密码，失败返回 null
     */
    public String resetPassword(String playerName) {
        if (authMeApi == null) {
            plugin.getLogger().severe("AuthMe API 未初始化，尝试命令方式");
            return resetPasswordByCommand(playerName);
        }

        String newPassword = generatePassword(8);

        try {
            // AuthMeApi.setPassword(playerName, password, hashPassword)
            // 第三个参数 true = AuthMe 自动哈希
            Method setPassword = authMeApi.getClass().getMethod("setPassword", String.class, String.class, boolean.class);
            boolean success = (boolean) setPassword.invoke(authMeApi, playerName, newPassword, true);

            if (success) {
                plugin.getLogger().info("已通过 AuthMe API 重置 " + playerName + " 的密码");
                return newPassword;
            } else {
                plugin.getLogger().warning("AuthMe API.setPassword 返回 false，尝试命令方式");
                return resetPasswordByCommand(playerName);
            }
        } catch (NoSuchMethodException e) {
            // 某些版本没有第三个参数
            try {
                Method setPassword = authMeApi.getClass().getMethod("setPassword", String.class, String.class);
                setPassword.invoke(authMeApi, playerName, newPassword);
                plugin.getLogger().info("已通过 AuthMe API (2参数) 重置 " + playerName + " 的密码");
                return newPassword;
            } catch (Exception e2) {
                plugin.getLogger().warning("AuthMe API 2参数调用失败: " + e2.getMessage());
                return resetPasswordByCommand(playerName);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("AuthMe API 调用异常: " + e.getMessage());
            return resetPasswordByCommand(playerName);
        }
    }

    /**
     * 备用方案：通过 authme 命令重置密码（必须在主线程执行）
     */
    private String resetPasswordByCommand(String playerName) {
        String newPassword = generatePassword(8);

        try {
            boolean success = Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "authme setpassword " + playerName + " " + newPassword
            );

            if (success) {
                plugin.getLogger().info("已通过命令重置 " + playerName + " 的密码");
                return newPassword;
            } else {
                plugin.getLogger().severe("authme setpassword 命令执行失败");
                return null;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("命令重置密码异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 检查 AuthMe API 是否可用
     */
    public boolean isApiReady() {
        return authMeApi != null;
    }

    /**
     * 生成随机密码
     */
    private String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
