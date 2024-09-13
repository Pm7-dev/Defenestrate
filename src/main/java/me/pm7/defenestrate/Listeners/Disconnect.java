
package me.pm7.defenestrate.Listeners;

import me.pm7.defenestrate.utils.BlockEntityManager;
import me.pm7.defenestrate.Defenestrate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zoglin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;


/*
        The reason for this listener is because of a strange event during the testing of this plugin, where if a player
        logged out while being held by another player, the server tried to disconnect the lower player, but
        unsuccessfully did so, which resulted in the lower player losing collision with the ground and just falling
        infinitely into the void until they relog. This was quite funny, but impractical for any sort of functioning
        server, so I removed it. If this feature is requested, I will add an option to enable it
*/

//        ^ get a load of this guy trying to make a serious comment in code :3

public class Disconnect implements Listener {
    private final Defenestrate plugin = Defenestrate.getPlugin();

    @EventHandler
    public void OnPlayerLeave (PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(p.getVehicle() instanceof Player vehicle) {
            vehicle.removePassenger(p);
        }

        List<Entity> eList = p.getNearbyEntities(1d, 2d, 1d);
        for(Entity entity : eList) {
            if(plugin.blocks().contains(entity.getUniqueId())) {
                new BlockEntityManager((Zoglin) entity);
            }
        }
    }
}