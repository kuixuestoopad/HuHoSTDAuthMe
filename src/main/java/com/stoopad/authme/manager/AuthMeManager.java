package com.stoopad.authme.manager;

import com.stoopad.authme.HuHoSTDAuthMe;
import org.bukkit.Bukkit;

import java.security.SecureRandom;

/**
 * AuthMe 密码管理器
 * 通过 AuthMe 控制台命令操作，不直接改数据库，100% 兼容
 */
public class AuthMeManager {

    private final HuHoSTDAuthMe plugin;
    private final SecureRandom random = new SecureRandom();

    private static final String PASSWORD_CHARS = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";

    public AuthMeManager(HuHoSTDAuthMe plugin) {
        this.plugin = plugin;
    }

    /**
     * 为玩家重置密码（通过 authme setpassword 命令）
     * 必须在主线程调用
     * @return 新密码，失败返回 null
     */
    public String resetPassword(String playerName) {
        String newPassword = generatePassword(8);

        try {
            // 使用 AuthMe 自带命令设置密码，AuthMe 自己处理哈希
            boolean success = Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "authme setpassword " + playerName + " " + newPassword
            );

            if (success) {
                plugin.getLogger().info("已通过 authme setpassword 重置 " + playerName + " 的密码");
                return newPassword;
            } else {
                plugin.getLogger().severe("authme setpassword 命令执行失败");
                return null;
            }
        } catch (Exception e) {
            plugin.getLogger().severe("重置密码异常: " + e.getMessage());
            return null;
        }
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
