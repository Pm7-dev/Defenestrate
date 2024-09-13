package me.pm7.defenestrate.Listeners;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class EnterVehicle implements Listener {
    private final Defenestrate plugin = Defenestrate.getPlugin();

    @EventHandler
    public void onEnterVehicle(VehicleEnterEvent e) {
        Entity en = e.getEntered();
        if (plugin.blocks().contains(en.getUniqueId())) {
            EntityType vt = e.getVehicle().getType();
            if(vt == EntityType.BOAT || vt == EntityType.CHEST_BOAT || vt == EntityType.MINECART) {
                e.setCancelled(true);
            }
        }
    }
}
