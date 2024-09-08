package me.pm7.defenestrate.Listeners;

import me.pm7.defenestrate.BlockEntityManager;
import me.pm7.defenestrate.Defenestrate;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.List;

// literally the exact same code as the disconnect lol

public class Portal implements Listener {
    private final Defenestrate plugin = Defenestrate.getPlugin();

    @EventHandler
    public void onPortal(EntityPortalEnterEvent e) {
        if(e.getEntity() instanceof Player p) {

            List<Entity> eList = p.getNearbyEntities(1d, 2d, 1d);
            for (Entity entity : eList) {
                if (plugin.blocks().contains(entity.getUniqueId())) {
                    new BlockEntityManager((Zoglin) entity);
                }
            }
        }
    }
}
