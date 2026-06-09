package com.stoopad.authme.listener;

import com.stoopad.authme.HuHoSTDAuthMe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final HuHoSTDAuthMe plugin;

    public ReloadCommand(HuHoSTDAuthMe plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length < 1 || !"reload".equalsIgnoreCase(args[0])) {
            sender.sendMessage(Component.text("用法: /huhostdauthme reload", NamedTextColor.YELLOW));
            return true;
        }

        if (!sender.hasPermission("qqauth.admin")) {
            sender.sendMessage(Component.text("§c你没有权限执行此命令"));
            return true;
        }

        plugin.reloadConfig();

        sender.sendMessage(Component.text("[HuHoSTDAuthMe] 配置已重载", NamedTextColor.GREEN));
        return true;
    }
}
