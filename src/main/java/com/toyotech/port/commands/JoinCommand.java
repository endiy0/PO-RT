package com.toyotech.port.commands;

import com.toyotech.port.PORTPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class JoinCommand implements CommandExecutor {
    private final PORTPlugin plugin;

    public JoinCommand(PORTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (!sender.isOp()) {
            sender.sendMessage("이 명령어를 사용할 권한이 없습니다.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("사용법: /join <사용자명>");
            return true;
        }

        String targetName = args[0];
        // Check if user has joined before using OfflinePlayer or our DataManager
        // DataManager is more reliable for name->uuid if we track it.
        // Prompt says: "If that user has joined this server at least once... If never joined before, deny"
        
        UUID targetUuid = plugin.getDataManager().getUuid(targetName);
        if (targetUuid == null) {
            // Fallback to Bukkit OfflinePlayer
             OfflinePlayer off = Bukkit.getOfflinePlayer(targetName);
             if (off.hasPlayedBefore() || off.isOnline()) {
                 targetUuid = off.getUniqueId();
             } else {
                 sender.sendMessage("해당 유저는 접속한 적이 없습니다.");
                 return true;
             }
        }

        // Load/create world
        // Prompt says: "create only if folder missing". getOrCreatePersonalWorld handles logic (creates if missing).
        // Wait, logic says "If never joined before, deny". We handled that.
        // So we just call getOrCreatePersonalWorld
        World w = plugin.getWorldManager().getOrCreatePersonalWorld(targetUuid);
        ((Player) sender).teleport(w.getSpawnLocation());
        sender.sendMessage(targetName + "님의 월드로 이동했습니다.");

        return true;
    }
}
