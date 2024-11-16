package me.thelpro.decoys.commands;

import me.thelpro.decoys.Decoy;
import me.thelpro.decoys.Decoys;
import me.thelpro.decoys.utils.DecoyItem;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainCommand implements TabExecutor {

    Decoys plugin = Decoys.plugin;
    FileConfiguration config = plugin.getConfig();

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (commandSender.hasPermission("decoys.admin")) {
            if (strings.length == 0) {
                commandSender.sendMessage("Run " + ChatColor.GOLD + "/decoys reload" + ChatColor.RESET + " to reload config.");
                commandSender.sendMessage("Run " + ChatColor.GOLD + "/decoys spawn" + ChatColor.RESET + " to spawn a decoy at your location.");
                return true;
            }
            if (strings[0].equalsIgnoreCase("reload")) {
                plugin.reloadConfig();
                commandSender.sendMessage("Plugin reloaded.");
                return true;
            } else if (strings[0].equalsIgnoreCase("spawn")) {
                if (commandSender instanceof Player player) {
                    Decoy decoy = new Decoy(player, player.getLocation());
                    decoy.spawnDecoy();
                    return true;
                }
            } else if (strings[0].equalsIgnoreCase("wipe")) {
                NPCRegistry registry = CitizensAPI.getNPCRegistry();
                ArrayList<NPC> npcs = (ArrayList<NPC>) registry.sorted();
                for (NPC npc : npcs) {
                    if (npc == null || npc.getEntity() == null) {
                        continue;
                    }
                    if (!npc.getEntity().hasMetadata("isDecoy"))
                        continue;
                    if (npc.getEntity().getMetadata("isDecoy").get(0).asBoolean()) {
                        npc.despawn(DespawnReason.PLUGIN);
                    }
                }
                return true;
            } else if (strings[0].equalsIgnoreCase("item")) {
                Player player = (Player) commandSender;
                player.getInventory().addItem(DecoyItem.getDecoyItem());
                player.sendMessage("Added decoy item to your inventory.");
                return true;
            }
        }

        commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        return true;
    }

    List<String> arguments = new ArrayList<String>();

    @Override
    public @Nullable List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        if (arguments.isEmpty()) {
            if (sender.hasPermission("decoys.admin")) {
                arguments.add("reload");
                arguments.add("spawn");
                arguments.add("wipe");
            }
        }
        List<String> result = new ArrayList<String>();
        if (args.length == 1) {
            for (String a : arguments) {
                if (a.toLowerCase().startsWith(args[0].toLowerCase()))
                    result.add(a);
            }
            return result;
        }
        return null;
    }
}
