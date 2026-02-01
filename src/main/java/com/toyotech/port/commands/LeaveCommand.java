package com.toyotech.port.commands;

import com.toyotech.port.PORTPlugin;
import com.toyotech.port.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {
    private final PORTPlugin plugin;

    public LeaveCommand(PORTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        Player p = (Player) sender;
        
        org.bukkit.Location bedSpawn = p.getBedSpawnLocation();
        if (bedSpawn != null) {
            p.teleport(bedSpawn);
            p.sendMessage("개인 월드로 이동했습니다.");
        } else {
            World pWorld = plugin.getWorldManager().getOrCreatePersonalWorld(p.getUniqueId());
            if (pWorld != null) {
                p.teleport(pWorld.getSpawnLocation());
                p.sendMessage("침대가 없어 메인 스폰포인트로 이동했습니다.");
            } else {
                p.sendMessage("이동할 수 있는 스폰 포인트가 없습니다.");
            }
        }

        return true;
    }
}
