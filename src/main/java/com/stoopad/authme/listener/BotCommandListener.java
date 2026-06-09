package com.stoopad.authme.listener;

import cn.huohuas001.huhobot.spigot.api.BotCustomCommand;
import com.alibaba.fastjson2.JSONObject;
import com.stoopad.authme.HuHoSTDAuthMe;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class BotCommandListener implements Listener {

    private final HuHoSTDAuthMe plugin;

    public BotCommandListener(HuHoSTDAuthMe plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBotCommand(BotCustomCommand event) {
        String command = event.getCommand();
        plugin.getLogger().info("[QQ命令] 收到: " + command);

        // 匹配"强制登陆"和"强制登录"两种写法
        String forceCmd = plugin.getForceLoginCommand();
        String forceAlt = forceCmd.replace("登陆", "登录");

        if (forceCmd.equals(command) || forceAlt.equals(command)) {
            handleForceLogin(event);
        } else if (plugin.getResetPasswordCommand().equals(command)) {
            handleResetPassword(event);
        }
    }

    /**
     * 强制登陆：绑定的 MC 账号在线时，调用 authme forcelogin 跳过验证
     */
    private void handleForceLogin(BotCustomCommand event) {
        event.setCancelled(true);

        JSONObject data = event.getData();
        JSONObject author = data.getJSONObject("author");
        String openId = author.getString("openId");
        plugin.getLogger().info("[强制登陆] QQ: " + openId);

        List<String> boundUsers = plugin.getBindManager().getBoundUsers(openId);
        if (boundUsers.isEmpty()) {
            event.respone(plugin.getMessage("no-bind"), "success");
            return;
        }

        StringBuilder result = new StringBuilder();

        for (String username : boundUsers) {
            Player player = Bukkit.getPlayerExact(username);
            if (player == null || !player.isOnline()) {
                result.append(plugin.getMessage("force-login-not-online")
                        .replace("{player}", username)).append("\n");
                continue;
            }

            boolean success = plugin.getForceLoginManager().authorize(username);
            if (success) {
                result.append(plugin.getMessage("force-login-success")
                        .replace("{player}", username)).append("\n");
                player.sendMessage(Component.text("§a[QQ] 你的账号已通过 QQ 强制登陆"));
            } else {
                result.append(plugin.getMessage("force-login-not-online")
                        .replace("{player}", username)).append("\n");
            }
        }

        // 回报
        event.respone(result.toString().trim(), "success");
    }

    /**
     * 重置密码：在线时通过命令重置，踢出显示新密码
     */
    private void handleResetPassword(BotCustomCommand event) {
        event.setCancelled(true);

        JSONObject data = event.getData();
        JSONObject author = data.getJSONObject("author");
        String openId = author.getString("openId");
        plugin.getLogger().info("[重置密码] QQ: " + openId);

        List<String> boundUsers = plugin.getBindManager().getBoundUsers(openId);
        plugin.getLogger().info("[重置密码] 绑定账号: " + boundUsers);

        if (boundUsers.isEmpty()) {
            event.respone(plugin.getMessage("no-bind"), "success");
            return;
        }

        // 在主线程执行：重置密码 + 踢出
        Bukkit.getScheduler().runTask(plugin, () -> {
            StringBuilder result = new StringBuilder();

            for (String username : boundUsers) {
                plugin.getLogger().info("[重置密码] 处理玩家: " + username);
                Player player = Bukkit.getPlayerExact(username);

                if (player == null || !player.isOnline()) {
                    plugin.getLogger().info("[重置密码] " + username + " 不在线");
                    result.append(plugin.getMessage("reset-password-not-online")
                            .replace("{player}", username)).append("\n");
                    continue;
                }

                plugin.getLogger().info("[重置密码] " + username + " 在线，开始重置");

                // 重置密码
                String newPassword = plugin.getAuthMeManager().resetPassword(username);
                plugin.getLogger().info("[重置密码] resetPassword 返回: " + (newPassword == null ? "NULL" : newPassword));

                if (newPassword == null) {
                    result.append(plugin.getMessage("reset-password-fail")
                            .replace("{player}", username)).append("\n");
                    continue;
                }

                // 踢出玩家，显示新密码
                String kickMsg = plugin.getMessage("kick-message")
                        .replace("{password}", newPassword)
                        .replace("{player}", username);
                player.kick(Component.text(kickMsg));
                plugin.getLogger().info("[重置密码] " + username + " 已踢出，新密码: " + newPassword);

                result.append(plugin.getMessage("reset-password-success")
                        .replace("{player}", username)).append("\n");
            }

            // 回报
            event.respone(result.toString().trim(), "success");
        });
    }
}
