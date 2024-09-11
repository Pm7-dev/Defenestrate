package me.pm7.defenestrate.Listeners;

import me.pm7.defenestrate.utils.BlockEntityManager;
import me.pm7.defenestrate.Defenestrate;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class Death implements Listener {
    private final Defenestrate plugin = Defenestrate.getPlugin();

    @EventHandler
    public void onPlayerDamage(PlayerDeathEvent e) {
        Player p = e.getEntity();
        if(!p.getPassengers().isEmpty()) {
            Entity passenger = p.getPassengers().getFirst();
            if(plugin.blocks().contains(passenger.getUniqueId())) {
                p.removePassenger(passenger);
                new BlockEntityManager((Zoglin) passenger);
            }
        }
    }
}
