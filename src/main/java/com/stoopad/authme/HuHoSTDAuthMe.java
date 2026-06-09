package com.stoopad.authme;

import com.stoopad.authme.manager.AuthMeManager;
import com.stoopad.authme.manager.ForceLoginManager;
import com.stoopad.authme.listener.BotCommandListener;
import com.stoopad.authme.listener.ReloadCommand;
import com.stoopad.qqwhitelist.manager.BindManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class HuHoSTDAuthMe extends JavaPlugin {

    private static HuHoSTDAuthMe instance;
    private AuthMeManager authMeManager;
    private ForceLoginManager forceLoginManager;
    private BindManager bindManager;
    private String forceLoginCommand;
    private String resetPasswordCommand;

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

        // 获取 BindManager（从 HuHoSTDWhiteList）
        try {
            var mainPlugin = (com.stoopad.qqwhitelist.QQWhitelistPlugin) whiteListPlugin;
            this.bindManager = mainPlugin.getBindManager();
        } catch (Exception e) {
            getLogger().severe("获取 BindManager 失败: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 读取命令名配置
        forceLoginCommand = getConfig().getString("force-login-command", "强制登陆");
        resetPasswordCommand = getConfig().getString("reset-password-command", "重置密码");

        this.authMeManager = new AuthMeManager(this);
        this.forceLoginManager = new ForceLoginManager(this);

        // 注册事件（通过 BotCustomCommand 事件处理，不需要 HuHoBot config 条目）
        getServer().getPluginManager().registerEvents(new BotCommandListener(this), this);

        // 注册命令
        getCommand("huhostdauthme").setExecutor(new ReloadCommand(this));

        getLogger().info("HuHoSTDAuthMe v1.1 已加载（强制登陆/重置密码 | 命令方式）");
        getLogger().info("命令关键词: " + forceLoginCommand + " / " + resetPasswordCommand);
    }

    @Override
    public void onDisable() {
        if (forceLoginManager != null) forceLoginManager.shutdown();
        getLogger().info("HuHoSTDAuthMe 已卸载");
    }

    /**
     * 获取回报消息
     */
    public String getMessage(String key) {
        return getConfig().getString("messages." + key, key);
    }

    public static HuHoSTDAuthMe getInstance() { return instance; }
    public AuthMeManager getAuthMeManager() { return authMeManager; }
    public ForceLoginManager getForceLoginManager() { return forceLoginManager; }
    public BindManager getBindManager() { return bindManager; }
    public String getForceLoginCommand() { return forceLoginCommand; }
    public String getResetPasswordCommand() { return resetPasswordCommand; }
}
