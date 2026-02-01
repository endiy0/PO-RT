package com.toyotech.port;

import com.toyotech.port.commands.HomeCommand;
import com.toyotech.port.commands.JoinCommand;
import com.toyotech.port.commands.LeaveCommand;
import com.toyotech.port.data.DataManager;
import com.toyotech.port.http.HttpApiServer;
import com.toyotech.port.items.TicketListener;
import com.toyotech.port.world.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

public class PORTPlugin extends JavaPlugin {

    private DataManager dataManager;
    private WorldManager worldManager;
    private HttpApiServer httpApiServer;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.dataManager = new DataManager(this);
        this.dataManager.load();

        this.worldManager = new WorldManager(this);
        getServer().getPluginManager().registerEvents(this.worldManager, this);
        getServer().getPluginManager().registerEvents(new TicketListener(this), this);
        // Also register a simple join listener to update name->uuid mapping
        getServer().getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
                dataManager.updateMapping(e.getPlayer().getName(), e.getPlayer().getUniqueId());
                // Teleport to personal world spawn (MVP behavior)
                e.getPlayer().teleport(worldManager.getOrCreatePersonalWorld(e.getPlayer().getUniqueId()).getSpawnLocation());
            }
        }, this);

        this.httpApiServer = new HttpApiServer(this);
        this.httpApiServer.start();

        getCommand("leave").setExecutor(new LeaveCommand(this));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("join").setExecutor(new JoinCommand(this));

        getLogger().info("PORT Plugin Enabled");
    }

    @Override
    public void onDisable() {
        if (httpApiServer != null) {
            httpApiServer.stop();
        }
        if (dataManager != null) {
            dataManager.save();
        }
        getLogger().info("PORT Plugin Disabled");
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public WorldManager getWorldManager() {
        return worldManager;
    }
}
