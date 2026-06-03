package com.stoopad.authme.manager;

import com.stoopad.authme.HuHoSTDAuthMe;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 强制登陆管理器
 * 通过 authme forcelogin 命令实现
 */
public class ForceLoginManager {

    private final HuHoSTDAuthMe plugin;

    // 已授权强制登陆的玩家名（小写）
    private final Set<String> forceLoginPlayers = ConcurrentHashMap.newKeySet();

    public ForceLoginManager(HuHoSTDAuthMe plugin) {
        this.plugin = plugin;
    }

    /**
     * 授权玩家强制登陆（玩家必须在线）
     * @return true=已授权，false=玩家不在线
     */
    public boolean authorize(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null || !player.isOnline()) {
            return false;
        }

        forceLoginPlayers.add(playerName.toLowerCase());

        // 在主线程执行 AuthMe 的 forcelogin 命令
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "authme forcelogin " + playerName);
                plugin.getLogger().info("已为 " + playerName + " 执行强制登陆");
            } catch (Exception e) {
                plugin.getLogger().severe("强制登陆命令执行失败: " + e.getMessage());
            }
        });

        // 5秒后清除授权
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            forceLoginPlayers.remove(playerName.toLowerCase());
        }, 20L * 5);

        return true;
    }

    /**
     * 检查玩家是否在强制登陆列表中
     */
    public boolean isForceLogin(String playerName) {
        return forceLoginPlayers.contains(playerName.toLowerCase());
    }

    public void shutdown() {
        forceLoginPlayers.clear();
    }
}
