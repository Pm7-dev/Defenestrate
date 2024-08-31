
package me.pm7.defenestrate.Listeners;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;


/*
        The reason for this listener is because of a strange event during the testing of this plugin, where if a player
        logged out while being held by another player, the server tried to disconnect the lower player, but
        unsuccessfully did so, which resulted in the lower player losing collision with the ground and just falling
        infinitely into the void until they relog. This was quite funny, and I was tempted to keep this bug in while the
        "breakThingsMode" setting was true. I realized that on any sort of server this would just be more annoying than
        anything, so I just patched it. If someone manages to read this and requests this to be added back as a feature,
        I will add it, possibly behind some "evenMoreBreakingThings" setting.
*/

//        ^ get a load of this guy trying to make a serious comment in code :P

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