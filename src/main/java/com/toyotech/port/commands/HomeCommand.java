package com.toyotech.port.commands;

import com.toyotech.port.PORTPlugin;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {
    private final PORTPlugin plugin;

    public HomeCommand(PORTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        Player p = (Player) sender;
        World w = plugin.getWorldManager().getOrCreatePersonalWorld(p.getUniqueId());
        p.teleport(w.getSpawnLocation());
        p.sendMessage("개인 월드로 이동했습니다.");

        return true;
    }
}
