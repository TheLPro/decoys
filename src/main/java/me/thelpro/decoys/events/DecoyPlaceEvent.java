package me.thelpro.decoys.events;

import me.thelpro.decoys.Decoy;
import me.thelpro.decoys.Decoys;
import me.thelpro.decoys.utils.DecoyItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.UUID;

public class DecoyPlaceEvent implements Listener {

    Decoys plugin = Decoys.plugin;
    FileConfiguration config = plugin.getConfig();

    private final HashMap<UUID, Long> cooldowns;

    public DecoyPlaceEvent() {
        this.cooldowns = new HashMap<>();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) return;
        if (e.getItem() == null) return;

        if (!e.getItem().isSimilar(DecoyItem.getDecoyItem())) {
            return;
        }
        e.setCancelled(true);
        if (!this.cooldowns.containsKey(e.getPlayer().getUniqueId())) {
            this.cooldowns.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());

            Player player = e.getPlayer();

            Block block = e.getClickedBlock();
            Location loc = block.getLocation();

            Decoy decoy = new Decoy(player, loc);
            decoy.spawnDecoy();
            return;
        } else {

            //in ms
            long timeElapsed = System.currentTimeMillis() - this.cooldowns.get(e.getPlayer().getUniqueId());
            long cooldown = config.getInt("spawn-cooldown") * 1000;

            if (timeElapsed >= cooldown) {
                this.cooldowns.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());

                Player player = e.getPlayer();
                e.setCancelled(true);

                Block block = e.getClickedBlock().getRelative(e.getBlockFace());
                Location loc = block.getLocation();

                Decoy decoy = new Decoy(player, loc);
                decoy.spawnDecoy();
                return;
            } else {
                int remaining = Math.round((float) (cooldown - timeElapsed) / 1000);
                e.getPlayer().sendMessage(ChatColor.RED + "This ability is on cooldown for another " + ChatColor.BOLD + remaining + ChatColor.RED + " second(s).");
                return;
            }

        }
    }

}