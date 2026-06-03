package com.stoopad.authme;

import com.stoopad.authme.manager.AuthMeManager;
import com.stoopad.authme.manager.ForceLoginManager;
import com.stoopad.authme.listener.BotCommandListener;
import com.stoopad.qqwhitelist.manager.BindManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuHoSTDAuthMe extends JavaPlugin {

    private static HuHoSTDAuthMe instance;
    private AuthMeManager authMeManager;
    private ForceLoginManager forceLoginManager;
    private BindManager bindManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // 检查 HuHoBot
        Plugin huhoBot = getServer().getPluginManager().getPlugin("HuHoBot");
        if (huhoBot == null) {
            getLogger().severe("HuHoBot 未安装！禁用 HuHoSTDAuthMe");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 检查 HuHoSTDWhiteList（依赖它的 BindManager）
        Plugin whiteListPlugin = getServer().getPluginManager().getPlugin("HuHoSTDWhiteList");
        if (whiteListPlugin == null) {
            getLogger().severe("HuHoSTDWhiteList 未安装！禁用 HuHoSTDAuthMe");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 检查 AuthMe
        Plugin authMe = getServer().getPluginManager().getPlugin("AuthMe");
        if (authMe == null) {
            getLogger().severe("AuthMe 未安装！禁用 HuHoSTDAuthMe");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            Class.forName("cn.huohuas001.huhobot.spigot.api.BotCustomCommand");
        } catch (ClassNotFoundException e) {
            getLogger().severe("HuHoBot API 加载失败: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 获取 BindManager（从 HuHoSTDWhiteList）
        try {
            var mainPlugin = (com.stoopad.qqwhitelist.QQWhitelistPlugin) whiteListPlugin;
            this.bindManager = mainPlugin.getBindManager();
        } catch (Exception e) {
            getLogger().severe("获取 BindManager 失败: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.authMeManager = new AuthMeManager(this);
        this.forceLoginManager = new ForceLoginManager(this);

        // 注册事件
        getServer().getPluginManager().registerEvents(new BotCommandListener(this), this);

        getLogger().info("HuHoSTDAuthMe 已加载（强制登陆/重置密码）");
    }

    @Override
    public void onDisable() {
        if (forceLoginManager != null) forceLoginManager.shutdown();
        getLogger().info("HuHoSTDAuthMe 已卸载");
    }

    public static HuHoSTDAuthMe getInstance() { return instance; }
    public AuthMeManager getAuthMeManager() { return authMeManager; }
    public ForceLoginManager getForceLoginManager() { return forceLoginManager; }
    public BindManager getBindManager() { return bindManager; }
}
