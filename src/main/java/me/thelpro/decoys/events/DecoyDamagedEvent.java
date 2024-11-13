package me.thelpro.decoys.events;

import me.thelpro.decoys.Decoys;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DecoyDamagedEvent implements Listener {

    Decoys plugin = Decoys.plugin;
    FileConfiguration config = plugin.getConfig();

    @EventHandler
    public void onDecoyDamagedEvent(EntityDamageEvent e) {
        if (e.getEntity() instanceof LivingEntity) {
            LivingEntity entity = (LivingEntity) e.getEntity();
            if (entity.isDead()) {
                return;
            }
                if (entity.hasMetadata("isDecoy")) {
                    if (entity.getMetadata("isDecoy").get(0).asBoolean()) {
                        entity.playHurtAnimation(0);
                        entity.remove();
                    }
                }
        }
    }
}