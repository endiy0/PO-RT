package com.toyotech.port.items;

import com.toyotech.port.PORTPlugin;
import com.toyotech.port.data.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.UUID;

public class TicketListener implements Listener {
    private final PORTPlugin plugin;
    public static final String INV_TITLE = "목적지 선택";
    public static final String TICKET_NAME = ChatColor.GREEN + "월드 접속권";
    public static final String ENTRANCE_SUFFIX = "의 월드 입장권";

    public TicketListener(PORTPlugin plugin) {
        this.plugin = plugin;
    }

    public static ItemStack getInvitationTicket() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(TICKET_NAME);
        meta.setLore(Arrays.asList("우클릭하여 월드를 선택하세요"));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                String displayName = item.getItemMeta().getDisplayName();
                
                if (displayName.equals(TICKET_NAME)) {
                    openGui(event.getPlayer());
                    event.setCancelled(true);
                } else if (displayName.endsWith(ENTRANCE_SUFFIX)) {
                    // Handle Entrance Ticket
                    handleEntranceTicket(event.getPlayer(), displayName, item);
                    event.setCancelled(true);
                }
            }
        }
    }

    private void handleEntranceTicket(Player p, String displayName, ItemStack item) {
        String stripped = ChatColor.stripColor(displayName);
        String targetName = stripped.replace(ENTRANCE_SUFFIX, "").trim();

        UUID targetUuid = plugin.getDataManager().getUuid(targetName);
        if (targetUuid == null) {
            p.sendMessage(ChatColor.RED + "플레이어 정보를 찾을 수 없습니다.");
            return;
        }

        World world = plugin.getWorldManager().getOrCreatePersonalWorld(targetUuid);
        if (world != null) {
            p.teleport(world.getSpawnLocation());
            p.sendMessage(ChatColor.GREEN + targetName + "님의 월드로 이동했습니다.");
            
            // Consume ticket
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                p.getInventory().removeItem(item);
            }
        } else {
            p.sendMessage(ChatColor.RED + "월드를 로드할 수 없습니다.");
        }
    }

    private void openGui(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, INV_TITLE);
        
        DataManager dm = plugin.getDataManager();
        for (String name : dm.getData().nameToUuid.keySet()) {
             if (name.equalsIgnoreCase(p.getName())) continue;

             ItemStack icon = new ItemStack(Material.PLAYER_HEAD);
             ItemMeta meta = icon.getItemMeta();
             meta.setDisplayName(ChatColor.YELLOW + name);
             icon.setItemMeta(meta);
             
             inv.addItem(icon);
        }
        
        p.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(INV_TITLE)) {
            event.setCancelled(true);
            
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            
            Player p = (Player) event.getWhoClicked();
            ItemStack clicked = event.getCurrentItem();
            
            if (clicked.hasItemMeta() && clicked.getItemMeta().hasDisplayName()) {
                String targetName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                
                // Give World Entrance Ticket
                giveEntranceTicket(p, targetName);
                
                // Consume Invitation Ticket
                // Use a safer removal logic or just removeItem which works by equals().
                // Our getInvitationTicket() creates a fresh item, exact NBT match might fail if plugin modified it?
                // But usually it's fine for simple items.
                p.getInventory().removeItem(getInvitationTicket());
                
                p.closeInventory();
                p.sendMessage(ChatColor.GREEN + targetName + "님의 월드 입장권을 획득했습니다!");
            }
        }
    }

    private void giveEntranceTicket(Player p, String targetName) {
        ItemStack ticket = new ItemStack(Material.PAPER);
        ItemMeta meta = ticket.getItemMeta();
        meta.setDisplayName(ChatColor.BLUE + targetName + ENTRANCE_SUFFIX);
        meta.setLore(Arrays.asList("우클릭하여 " + targetName + "님의 월드로 이동"));
        ticket.setItemMeta(meta);
        p.getInventory().addItem(ticket);
    }
}
