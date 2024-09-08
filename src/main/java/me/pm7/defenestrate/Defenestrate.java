package me.pm7.defenestrate;

import me.pm7.defenestrate.Commands.dsettings;
import me.pm7.defenestrate.Listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class Defenestrate extends JavaPlugin {

    private static Defenestrate plugin;

    @Override
    public void onEnable() {
        plugin = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        killRemainingBlocks();

        getServer().getPluginManager().registerEvents(new PreventZoglinInteraction(), this);
        getServer().getPluginManager().registerEvents(new Disconnect(), this);
        getServer().getPluginManager().registerEvents(new Launch(), this);
        getServer().getPluginManager().registerEvents(new Portal(), this);
        getServer().getPluginManager().registerEvents(new Death(), this);
        getCommand("dsettings").setTabCompleter(new dsettings());
        getCommand("dsettings").setExecutor(new dsettings());


        int spawnProt = Bukkit.getServer().getSpawnRadius();
        if(spawnProt > 0) {
            getLogger().log(Level.WARNING, "This server has spawn protection enabled! Defenestrate will not be able to be used by non-operators until they are " + spawnProt + " blocks away from spawn!");
        }
    }

    public Entity getPassenger(Player p) {
        if(p != null) {
            List<Entity> passengers = p.getPassengers();
            if(passengers.size() > 1) {
                while (passengers.size() > 1) {
                    p.removePassenger(passengers.get(1));
                    passengers = p.getPassengers();
                }
            }
            if(!passengers.isEmpty()) {return passengers.getFirst();}
        }
        return null;
    }

    // If there are any custom block entities when the plugin starts, they should be removed.
    private void killRemainingBlocks() {
        for (World w : Bukkit.getWorlds()) {
            for(Entity e : w.getEntities()) {
                if(!e.getPassengers().isEmpty() && e.getType() == EntityType.ZOGLIN) {
                    Entity in = e.getPassengers().getFirst();
                    if(in.getType() == EntityType.INTERACTION && !in.getPassengers().isEmpty()) {
                        Entity bd = in.getPassengers().getFirst();
                        if(bd instanceof BlockDisplay) {
                            e.getPassengers().getFirst().getPassengers().getFirst().remove();
                            e.getPassengers().getFirst().remove();
                            e.remove();
                        }
                    }
                }
            }
        }
    }

    private final HashSet<UUID> blocks = new HashSet<>();
    public HashSet<UUID> blocks() {
        return blocks;
    }
    public void registerBlock(UUID uuid) {
        blocks.add(uuid);
    }
    public void unregisterBlock(UUID uuid) {
        blocks.remove(uuid);
    }

    public static Defenestrate getPlugin() {return plugin;}
}
