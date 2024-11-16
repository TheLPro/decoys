package me.thelpro.decoys.utils;

import me.thelpro.decoys.Decoys;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DecoyItem {

    static Decoys plugin = Decoys.plugin;
    static FileConfiguration config = plugin.getConfig();

    public static ItemStack getDecoyItem() {

        ItemStack item = new ItemStack(Material.GHAST_SPAWN_EGG);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("item-name")));

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.WHITE + "Place to summon");
        lore.add(ChatColor.WHITE + "a Decoy for 10s");
        lore.add(ChatColor.GRAY + "ᴄᴏᴏʟᴅᴏᴡɴ: 30s");
        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }
}