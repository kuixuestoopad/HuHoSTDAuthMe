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

        switch (command) {
            case "强制登陆", "强制登录" -> handleForceLogin(event);
            case "重置密码" -> handleResetPassword(event);
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
            event.respone("❌ 你还没有绑定任何游戏账号", "text");
            return;
        }

        StringBuilder result = new StringBuilder();

        for (String username : boundUsers) {
            Player player = Bukkit.getPlayerExact(username);
            if (player == null || !player.isOnline()) {
                result.append("⚠ ").append(username).append(" 不在线\n");
                continue;
            }

            boolean success = plugin.getForceLoginManager().authorize(username);
            if (success) {
                result.append("✅ ").append(username).append(" 已强制登陆\n");
                player.sendMessage(Component.text("§a[QQ] 你的账号已通过 QQ 强制登陆"));
            } else {
                result.append("❌ ").append(username).append(" 强制登陆失败\n");
            }
        }

        event.respone(result.toString().trim(), "text");
    }

    /**
     * 重置密码：在线时通过 AuthMe API 设置新密码，踢出显示新密码
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
            event.respone("❌ 你还没有绑定任何游戏账号", "text");
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
                    result.append("⚠ ").append(username).append(" 不在线\n");
                    continue;
                }

                plugin.getLogger().info("[重置密码] " + username + " 在线，开始重置");

                // 重置密码
                String newPassword = plugin.getAuthMeManager().resetPassword(username);
                plugin.getLogger().info("[重置密码] resetPassword 返回: " + (newPassword == null ? "NULL" : newPassword));

                if (newPassword == null) {
                    result.append("❌ ").append(username).append(" 密码重置失败\n");
                    continue;
                }

                // 踢出玩家，显示新密码
                String kickMsg = "§a§l密码已重置！\n\n§e新密码: §f§l" + newPassword + "\n\n§7请使用新密码重新登录";
                player.kick(Component.text(kickMsg));
                plugin.getLogger().info("[重置密码] " + username + " 已踢出，新密码: " + newPassword);

                result.append("✅ ").append(username).append(" 密码已重置\n");
                result.append("   新密码: ").append(newPassword).append("\n");
            }

            String response = result.toString().trim();
            plugin.getLogger().info("[重置密码] 回复QQ: " + response);
            event.respone(response, "text");
        });
    }
}
