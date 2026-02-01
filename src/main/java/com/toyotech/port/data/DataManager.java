package com.toyotech.port.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.toyotech.port.PORTPlugin;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class DataManager {
    private final PORTPlugin plugin;
    private final File file;
    private final Gson gson;
    private Data data;

    public DataManager(PORTPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.data = new Data();
    }

    public void load() {
        if (!file.exists()) {
            save(); // Create empty
            return;
        }
        try (Reader reader = new FileReader(file)) {
            data = gson.fromJson(reader, Data.class);
            if (data == null) data = new Data();
            if (data.players == null) data.players = new HashMap<>();
            if (data.nameToUuid == null) data.nameToUuid = new HashMap<>();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not load data.json", e);
        }
    }

    public void save() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        // Async save to avoid lag, or sync if critical? 
        // For safety/simplicity in this context, we'll do sync or just carefully. 
        // The prompt asks for "Persistence ... Save onDisable and after every mutation".
        // Sync is safer for data integrity on small scale.
        try (Writer writer = new FileWriter(file)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data.json", e);
        }
    }

    public Data getData() {
        return data;
    }

    public UUID getUuid(String username) {
        String uuidStr = data.nameToUuid.get(username.toLowerCase());
        return uuidStr != null ? UUID.fromString(uuidStr) : null;
    }

    public void updateMapping(String username, UUID uuid) {
        data.nameToUuid.put(username.toLowerCase(), uuid.toString());
        save();
    }

    public PlayerData getPlayerData(UUID uuid) {
        return data.players.computeIfAbsent(uuid.toString(), k -> new PlayerData("pworld_" + uuid.toString()));
    }

    public static class Data {
        public Map<String, PlayerData> players = new HashMap<>();
        public Map<String, String> nameToUuid = new HashMap<>();
    }

    public static class PlayerData {
        public String worldName;
        public int tickets;

        public PlayerData(String worldName) {
            this.worldName = worldName;
            this.tickets = 0;
        }
    }
}
