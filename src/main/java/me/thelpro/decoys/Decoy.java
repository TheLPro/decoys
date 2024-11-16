package me.thelpro.decoys;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.Navigator;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.LookClose;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitTask;

public class Decoy {

    Decoys plugin = Decoys.plugin;
    FileConfiguration config = plugin.getConfig();

    Player player;
    Location loc;

    public Decoy(Player player, Location loc) {
        this.player = player;
        this.loc = loc;
    }

    public void spawnDecoy() {
        String playername;
        if (config.getBoolean("use-display-name")) {
            playername = player.getDisplayName();
        } else {
            playername = player.getName();
        }
        String rawname = config.getString("decoy-name");
        String decoyname = rawname.replace("$player", playername);

        NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, decoyname);

        npc.getOrAddTrait(SkinTrait.class).setSkinName(player.getName());

        if (player.getEquipment().getHelmet() != null)
            npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.HELMET, player.getEquipment().getHelmet());
        if (player.getEquipment().getChestplate() != null)
            npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.CHESTPLATE, player.getEquipment().getChestplate());
        if (player.getEquipment().getLeggings() != null)
            npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.LEGGINGS, player.getEquipment().getLeggings());
        if (player.getEquipment().getBoots() != null)
            npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.BOOTS, player.getEquipment().getBoots());

        if (player.getEquipment().getItemInMainHand().getType() != Material.AIR)
            npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.HAND, getPreferredItem(player));

        npc.spawn(loc);

        Boolean random = false;
        if (config.getBoolean("pathfinding")) {
            if (config.getString("pathfinding-method").equalsIgnoreCase("follownearest")) {
                random = false;
                followClosestPlayer(npc);
            } else if (config.getString("pathfinding-method").equalsIgnoreCase("random")) {
                random = true;
                startMovingRandomly(npc);
            }
        }

        if (config.getBoolean("lookclose") && !random)
            npc.getOrAddTrait(LookClose.class).lookClose(true);

        npc.getEntity().setMetadata("isDecoy", new FixedMetadataValue(plugin, true));

        npc.setProtected(false);

        startDespawnCooldown(npc);

    }

    BukkitTask followTask;

    public void followClosestPlayer(NPC npc) {
        followTask = Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("Decoys"), () -> {
            // Check if the NPC entity is still valid
            if (npc == null || !npc.isSpawned() || npc.getEntity() == null) {
                // Cancel the task if the NPC is no longer valid
                followTask.cancel();
                return;
            }

            Player closestPlayer = null;
            double closestDistance = Double.MAX_VALUE;

            for (Player player : Bukkit.getOnlinePlayers()) {
                double distance = npc.getEntity().getLocation().distance(player.getLocation());

                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestPlayer = player;
                }
            }

            if (closestPlayer != null) {
                Navigator navigator = npc.getNavigator();
                navigator.setTarget(closestPlayer, false);

                NavigatorParameters params = navigator.getDefaultParameters();

                params.range(10);
                if (config.getInt("speed-modifier") > 0)
                    params.speedModifier((float) config.getDouble("speed-modifier", 1.0));
            }
        }, 0L, 20L);
    }

    BukkitTask randomTask;

    private void startMovingRandomly(NPC npc) {
        randomTask = Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("Decoys"), () -> {

            if (npc == null || !npc.isSpawned() || npc.getEntity() == null) {
                // Cancel the task if the NPC is no longer valid
                randomTask.cancel();
                return;
            }

            if (!npc.getNavigator().isNavigating()) {
                Location currentLocation = npc.getEntity().getLocation();
                World world = currentLocation.getWorld();

                // Generate random coordinates within 25 blocks for x, z; retain the current y level
                double x = currentLocation.getX() + (Math.random() * 100 - 50);
                double z = currentLocation.getZ() + (Math.random() * 100 - 50);
                Location targetLocation = new Location(world, x, currentLocation.getY(), z);

                // Check if target location has two air blocks above it
                if (!isLocationClear(targetLocation)) {
                    return;
                }

                Navigator navigator = npc.getNavigator();
                NavigatorParameters params = navigator.getDefaultParameters();
                if (config.getInt("speed-modifier") > 0)
                    params.speedModifier((float) config.getDouble("speed-modifier", 1.0));
                params.range(25);  // Restrict to 25 blocks
                params.useNewPathfinder(false);  // Enable pathfinding

                navigator.setTarget(targetLocation);  // Move towards the target location
            }
        }, 0L, 1L); // Run every 20 ticks (1 second)
    }

    // Utility method to check if location has two air blocks above
    private boolean isLocationClear(Location loc) {
        World world = loc.getWorld();
        return world.getBlockAt(loc).isPassable() &&
                world.getBlockAt(loc.clone().add(0, 1, 0)).isPassable() &&
                world.getBlockAt(loc.clone().add(0, 2, 0)).isPassable();
    }

    public ItemStack getPreferredItem(Player player) {
        ItemStack item = null;

        if (config.getString("decoy-item").equalsIgnoreCase("slot"))
            item = player.getInventory().getItem(0);
        if (config.getString("decoy-item").equalsIgnoreCase("none"))
            item = new ItemStack(Material.AIR);

        return item;
    }

    BukkitTask despawnTask;

    private void startDespawnCooldown(NPC npc) {
        despawnTask = Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("Decoys"), () -> {
            if (npc == null || !npc.isSpawned() || npc.getEntity() == null) {
                // Cancel the task if the NPC is no longer valid
                despawnTask.cancel();
                return;
            }

            LivingEntity entity = (LivingEntity) npc.getEntity();
            entity.playHurtAnimation(0);
            entity.remove();

        }, 400L);
    }
}