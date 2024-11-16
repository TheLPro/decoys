package me.thelpro.decoys.events;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.thelpro.decoys.Decoy;
import me.thelpro.decoys.Decoys;
import me.thelpro.decoys.utils.DecoyItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
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

            if (!canSpawnDecoy(loc)) return;

            Decoy decoy = new Decoy(player, loc);
            decoy.spawnDecoy();

            e.getItem().setAmount(e.getItem().getAmount() - 1);
            e.getPlayer().setCooldown(Material.GHAST_SPAWN_EGG, config.getInt("spawn-cooldown") * 20);

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

                if (!canSpawnDecoy(loc)) return;

                Decoy decoy = new Decoy(player, loc);
                decoy.spawnDecoy();

                e.getItem().setAmount(e.getItem().getAmount() - 1);
                e.getPlayer().setCooldown(Material.GHAST_SPAWN_EGG, config.getInt("spawn-cooldown") * 20);

                return;
            } else {
                int remaining = Math.round((float) (cooldown - timeElapsed) / 1000);
                if (!config.getString("spawn-cooldown-message").isBlank()) {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("spawn-cooldown-message").replace("$remaining", String.valueOf(remaining))));
                }
                return;
            }

        }
    }

    private boolean canSpawnDecoy(Location loc) {
        boolean canSpawn = true;

        com.sk89q.worldedit.util.Location location = BukkitAdapter.adapt(loc);
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(loc.getWorld());
        RegionContainer rc = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rm = rc.get(world);
        ArrayList<String> regions = new ArrayList<>();
        regions.add("spawn");
        regions.add("spawnsummer");
        regions.add("plots");
        regions.add("shulkerrooms");

        RegionQuery rq = rc.createQuery();
        ApplicableRegionSet set = rq.getApplicableRegions(location);

        for (ProtectedRegion region : set) {
            for (String regionName : regions) {
                if (regionName.equalsIgnoreCase(region.getId())) {
                    canSpawn = false;
                    break;
                }
            }
        }
        return canSpawn;
    }
}