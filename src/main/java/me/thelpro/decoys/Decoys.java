package me.thelpro.decoys;

import me.thelpro.decoys.commands.MainCommand;
import me.thelpro.decoys.events.DecoyDamagedEvent;
import me.thelpro.decoys.events.DecoyPlaceEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class Decoys extends JavaPlugin {

    public static Decoys plugin;

    @Override
    public void onEnable() {

        reloadConfig();
        saveDefaultConfig();

        plugin = this;

        getCommand("decoys").setExecutor(new MainCommand());
        getServer().getPluginManager().registerEvents(new DecoyPlaceEvent(), this);
        getServer().getPluginManager().registerEvents(new DecoyDamagedEvent(), this);

    }

    @Override
    public void onDisable() {
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
    }

}