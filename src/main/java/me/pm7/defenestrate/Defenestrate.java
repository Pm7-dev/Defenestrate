package me.pm7.defenestrate;

import me.pm7.defenestrate.Commands.settings;
import me.pm7.defenestrate.Listeners.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Defenestrate extends JavaPlugin {

    private static Defenestrate plugin;

    @Override
    public void onEnable() {
        plugin = this;

        getConfig().options().copyDefaults();
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new Launch(), this);
        getServer().getPluginManager().registerEvents(new Disconnect(), this);
        getCommand("settings").setExecutor(new settings());
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
            if(!passengers.isEmpty()) {return passengers.get(0);}
        }
        return null;
    }


    public static Defenestrate getPlugin() {return plugin;}
}
