package com.stoopad.authme.manager;

import com.stoopad.authme.HuHoSTDAuthMe;
import org.bukkit.Bukkit;

import java.security.SecureRandom;

/**
 * AuthMe 密码管理器 v1.1
 * 纯命令方式，移除反射 API
 */
public class AuthMeManager {

    private final HuHoSTDAuthMe plugin;
    private final SecureRandom random = new SecureRandom();

    private static final String PASSWORD_CHARS = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";

    public AuthMeManager(HuHoSTDAuthMe plugin) {
        this.plugin = plugin;
    }

    /**
     * 通过 authme password 命令重置密码
     * @return 新密码，失败返回 null
     */
    public String resetPassword(String playerName) {
        String newPassword = generatePassword(8);
        plugin.getLogger().info("[重置密码] 玩家=" + playerName + " 新密码=" + newPassword);

        try {
            boolean success = Bukkit.dispatchCommand(
                    Bukkit.getConsoleSender(),
                    "authme password " + playerName + " " + newPassword
            );
            plugin.getLogger().info("[重置密码] authme password 命令返回: " + success);
            if (success) return newPassword;
        } catch (Exception e) {
            plugin.getLogger().severe("[重置密码] 命令异常: " + e.getMessage());
        }

        plugin.getLogger().severe("[重置密码] 失败！");
        return null;
    }

    private String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
