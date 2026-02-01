package com.toyotech.port.world;

import com.toyotech.port.PORTPlugin;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class WorldManager implements Listener {
    private final PORTPlugin plugin;
    private final Map<String, BukkitTask> unloadTasks = new HashMap<>();
    private final String mainWorldName;
    private final String sharedNetherName;
    private final String sharedEndName;

    public WorldManager(PORTPlugin plugin) {
        this.plugin = plugin;
        this.mainWorldName = plugin.getConfig().getString("mainWorld", "world");
        this.sharedNetherName = mainWorldName + "_nether";
        this.sharedEndName = mainWorldName + "_the_end";
    }

    public World getOrCreatePersonalWorld(UUID uuid) {
        String worldName = "pworld_" + uuid.toString();
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            // Check if folder exists
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            boolean exists = worldFolder.exists();

            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(World.Environment.NORMAL);
            creator.type(WorldType.NORMAL);
            
            if (!exists) {
                creator.seed(new Random().nextLong());
                plugin.getLogger().info("Creating new personal world for " + uuid);
            } else {
                plugin.getLogger().info("Loading existing personal world for " + uuid);
            }
            
            world = Bukkit.createWorld(creator);
        }

        // Cancel any pending unload for this world
        cancelUnload(worldName);

        return world;
    }

    public void scheduleUnload(String worldName) {
        // Never unload main, nether, end
        if (worldName.equals(mainWorldName) || worldName.equals(sharedNetherName) || worldName.equals(sharedEndName)) return;
        if (!worldName.startsWith("pworld_")) return;

        if (Bukkit.getWorld(worldName) == null) return;
        if (unloadTasks.containsKey(worldName)) return; // Already scheduled

        plugin.getLogger().info("Scheduling unload for " + worldName + " in 30 seconds.");
        
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            World w = Bukkit.getWorld(worldName);
            if (w != null && w.getPlayers().isEmpty()) {
                plugin.getLogger().info("Unloading " + worldName);
                Bukkit.unloadWorld(w, true);
            }
            unloadTasks.remove(worldName);
        }, 20 * 30); // 30 seconds

        unloadTasks.put(worldName, task);
    }

    public void cancelUnload(String worldName) {
        BukkitTask task = unloadTasks.remove(worldName);
        if (task != null) {
            task.cancel();
            plugin.getLogger().info("Cancelled unload for " + worldName);
        }
    }

    // --- Listeners ---

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        checkWorldUnload(event.getPlayer().getWorld());
    }

    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        // Left world
        checkWorldUnload(event.getFrom());
        
        // Entered world
        cancelUnload(event.getPlayer().getWorld().getName());
    }

    private void checkWorldUnload(World world) {
        if (world.getPlayers().isEmpty() || (world.getPlayers().size() == 1 && !world.getPlayers().get(0).isOnline())) {
             // The player count check might be tricky during event execution (player might still be in list), 
             // but usually getPlayers() reflects current state or we check if empty.
             // safely schedule. The task will double check.
             scheduleUnload(world.getName());
        }
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        World from = event.getFrom().getWorld();
        if (from.getName().startsWith("pworld_")) {
            // Redirect to shared dimensions
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
                World nether = Bukkit.getWorld(sharedNetherName);
                if (nether == null) return; // Should exist if main world loaded
                // Simple logic: send to spawn. 
                // Advanced: calculate coords. Request says "shared nether spawn location".
                // We need to set useTravelAgent(true) to find a portal or just teleport to spawn?
                // "set destination to shared nether spawn location" -> Strict interpretation.
                event.setTo(nether.getSpawnLocation());
                // Paper/Spigot portal event handling can be tricky.
                // If we set To, we might want to disable creation or search radius if we literally just want spawn.
                // But usually players want a portal to appear.
                // Request: "set destination to shared nether spawn location"
                // We will comply strictly.
            } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                World end = Bukkit.getWorld(sharedEndName);
                if (end != null) {
                    event.setTo(end.getSpawnLocation());
                }
            }
        } else if (from.getName().equals(sharedNetherName) || from.getName().equals(sharedEndName)) {
             // Return from shared dimension.
             // If they came from pworld, where do they go?
             // "Track per-player last overworld location" is in the prompt.
             // I need to implement tracking.
             // For now, default to main world spawn or pworld spawn?
             // Prompt: "Track per-player last overworld location before sending to shared nether/end."
             
             // I'll need a Map<UUID, Location> lastOverworldLocation.
        }
    }
    
    // We need tracking.
    private final Map<UUID, Location> lastOverworldLocs = new HashMap<>();

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        // Track valid overworld locations
        if (event.getFrom().getWorld().getEnvironment() == World.Environment.NORMAL) {
             lastOverworldLocs.put(event.getPlayer().getUniqueId(), event.getFrom());
        }
        
        // Handling return from nether/end is complex in PlayerPortalEvent because usually PortalEvent triggers TeleportEvent.
        // But for PortalEvent, we modify the destination.
    }
    
    // Updated onPortal to use tracking
    // But wait, PlayerPortalEvent is for *using* a portal.
    // If I am in Nether and enter portal, I trigger PlayerPortalEvent.
    // I should set destination to lastOverworldLocs if available.
    
    @EventHandler
    public void onPortalRouting(PlayerPortalEvent event) {
        World from = event.getFrom().getWorld();
        Player p = event.getPlayer();

        if (from.getEnvironment() == World.Environment.NORMAL) {
            // Going to Nether or End
            lastOverworldLocs.put(p.getUniqueId(), event.getFrom()); // Save location
            
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
                World nether = Bukkit.getWorld(sharedNetherName);
                if (nether != null) event.setTo(nether.getSpawnLocation());
            } else if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                 World end = Bukkit.getWorld(sharedEndName);
                 if (end != null) event.setTo(end.getSpawnLocation());
            }
        } else {
            // Returning from Nether/End
            if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL || 
                event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
                
                Location last = lastOverworldLocs.get(p.getUniqueId());
                if (last != null) {
                    event.setTo(last);
                } else {
                    // Fallback to pworld if exists, else main world
                    World pworld = Bukkit.getWorld("pworld_" + p.getUniqueId());
                    if (pworld != null) event.setTo(pworld.getSpawnLocation());
                    else event.setTo(Bukkit.getWorld(mainWorldName).getSpawnLocation());
                }
            }
        }
    }
}
