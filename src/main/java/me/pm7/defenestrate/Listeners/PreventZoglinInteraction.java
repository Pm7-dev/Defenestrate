package me.pm7.defenestrate.Listeners;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class PreventZoglinInteraction implements Listener {
    private final Defenestrate plugin = Defenestrate.getPlugin();

    @EventHandler
    public void onZoglinAttack(EntityDamageByEntityEvent e) {
        if(plugin.blocks().contains(e.getDamager().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onTargetBlock(EntityTargetLivingEntityEvent e) {
        if(e.getTarget() == null) {return;}
        if(plugin.blocks().contains(e.getTarget().getUniqueId())) {
            e.setCancelled(true);
        }
    }
}
