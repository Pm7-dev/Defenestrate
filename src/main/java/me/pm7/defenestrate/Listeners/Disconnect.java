
package me.pm7.defenestrate.Listeners;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;


public class Disconnect implements Listener {
    private final Defenestrate plugin = Defenestrate.getPlugin();

    @EventHandler
    public void OnPlayerLeave (PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(p.getVehicle() instanceof Player) {
            Player vehicle = (Player) p.getVehicle();
            vehicle.removePassenger(p);
        }
        if(plugin.getPassenger(p) instanceof Player) {
            p.removePassenger(plugin.getPassenger(p));
        }
    }
}