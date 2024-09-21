package me.pm7.defenestrate.Listeners;

import me.pm7.defenestrate.utils.BlockEntityManager;
import me.pm7.defenestrate.Defenestrate;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Launch implements Listener {
    private final Defenestrate plugin = Defenestrate.getPlugin();
    FileConfiguration config = plugin.getConfig();
    List<UUID> debounceList = new ArrayList<>(); // debounce for picking up blocks, since it ghost creates a left click event when you are looking at air sometimes

    // Picking up Players and Entities
    @EventHandler
    public void interactEntityListener(PlayerInteractEntityEvent e) {

        // This is stupid. Why does it run for each hand
        if(e.getHand() == EquipmentSlot.OFF_HAND) return;

        Player p = e.getPlayer();
        if(p.getGameMode() == GameMode.SPECTATOR) {return;}

        // do the debounce which needs to be here for some stupid reason
        UUID uuid = p.getUniqueId();
        if(debounceList.contains(uuid)) return;
        debounceList.add(uuid);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> debounceList.remove(uuid), 3L);


        // permissions
        if(e.getRightClicked() instanceof Player) {
            if(!config.getBoolean("playerThrowEnabled")) { return; }
            if(config.getBoolean("throwPlayersRequiresPermission")) {
                if(!p.hasPermission("defenestrate.players") && !p.hasPermission("defenestrate.all")) {
                    return;
                }
            }
        } else if(!(e.getRightClicked() instanceof Interaction)){
            if(!config.getBoolean("entityThrowEnabled")) { return; }
            if(config.getBoolean("throwEntitiesRequiresPermission")) {
                if(!p.hasPermission("defenestrate.entities") && !p.hasPermission("defenestrate.all")) {
                    return;
                }
            }
        }

        // check if the click is valid
        if(!p.isSneaking() || !p.getInventory().getItemInMainHand().getType().isAir()) { return;}

        Entity clicked = e.getRightClicked();
        if(config.getStringList("blockedEntities").contains(clicked.getType().toString())) { return; }

        // Check if the entity is inside spawn protection
        if(inSpawnProt(clicked.getLocation())) {
            if(!p.isOp()) {
                p.sendMessage(ChatColor.RED + "This entity is in spawn protection!");
                return;
            }
        }

        // Check for players stealing blocks off of others' heads
        if(clicked.getType() == EntityType.INTERACTION) {

            // permissions
            if(!config.getBoolean("blockThrowEnabled")) { return; }
            if(config.getBoolean("throwBlocksRequiresPermission")) {
                if(!p.hasPermission("defenestrate.blocks") && !p.hasPermission("defenestrate.all")) {
                    return;
                }
            }

            if(clicked.isInsideVehicle() && plugin.blocks().contains(clicked.getVehicle().getUniqueId())) {

                // Make sure the player is not already carrying something
                Entity passenger = plugin.getPassenger(p);
                if (passenger != null) { return; }

                if(clicked.getVehicle().isInsideVehicle()) {
                    if(config.getBoolean("allowStealing")) {
                        clicked.getVehicle().getVehicle().removePassenger(clicked.getVehicle());
                    } else {
                        return;
                    }
                }
                p.addPassenger(clicked.getVehicle());

            }
        } else {

            // Make sure the player is not already carrying something
            Entity passenger = plugin.getPassenger(p);
            if (passenger == null) {
                if(clicked.isInsideVehicle()) {
                    if(config.getBoolean("allowStealing")) {
                        p.addPassenger(clicked);
                    } else {
                        return;
                    }
                } else {
                    p.addPassenger(clicked);
                }
            }
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void interactListener(PlayerInteractEvent e) {

        // This is stupid. Why does it run for each hand
        if(e.getHand() == EquipmentSlot.OFF_HAND) return;

        Player p = e.getPlayer();
        if(p.getGameMode() == GameMode.SPECTATOR) {return;}

        // do the debounce which needs to be here for some stupid reason
        UUID uuid = p.getUniqueId();
        if(debounceList.contains(uuid)) return;
        debounceList.add(uuid);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> debounceList.remove(uuid), 3L);

        Entity passenger = plugin.getPassenger(p);
        Action action = e.getAction();

        // Picking up Blocks
        if(passenger == null) {
            if(action != Action.RIGHT_CLICK_BLOCK) {return;}
            if (!p.isSneaking() || !p.getInventory().getItemInMainHand().getType().isAir()) { return;}
            if(p.getGameMode() == GameMode.ADVENTURE) {return;}

            // permissions
            if(!config.getBoolean("blockThrowEnabled")) { return; }
            if(config.getBoolean("throwBlocksRequiresPermission")) {
                if(!p.hasPermission("defenestrate.blocks") && !p.hasPermission("defenestrate.all")) {
                    return;
                }
            }

            Block b = e.getClickedBlock();
            if (b == null) { return; }
            if(config.getStringList("blockedBlocks").contains(b.getType().toString())) { return; }

            // Check if the block is inside spawn protection
            if(inSpawnProt(b.getLocation())) {
                if(!p.isOp()) {
                    p.sendMessage(ChatColor.RED + "This block is in spawn protection!");
                    return;
                }
            }

            if(config.getBoolean("oldBlockHandling")) {

                // Using the older falling block method
                FallingBlock fallingBlock = p.getWorld().spawnFallingBlock(p.getLocation().add(0, 1.5, 0), b.getBlockData());
                p.addPassenger(fallingBlock);

            } else {

                // New method of making blocks work
                World world = p.getWorld();
                Location loc = p.getLocation();
                loc.setY(500.0d);

                Zoglin base = (Zoglin) world.spawnEntity(loc, EntityType.ZOGLIN);
                base.setBaby();
                base.setRemoveWhenFarAway(false);
                base.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, PotionEffect.INFINITE_DURATION, 1, false, false));
                base.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 10, false, false));
                base.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 10, false, false));
                base.setSilent(true);
                base.getAttribute(Attribute.GENERIC_SCALE).setBaseValue(0.01d);
                plugin.registerBlock(base.getUniqueId());

                Interaction hitbox = (Interaction) world.spawnEntity(loc, EntityType.INTERACTION);
                hitbox.setRotation(0, 0);
                base.addPassenger(hitbox);

                BlockDisplay block = (BlockDisplay) world.spawnEntity(loc, EntityType.BLOCK_DISPLAY);
                block.setRotation(0, 0); // paper servers throw a fit when you don't have this (the block starts facing the same direction the player is for some reason)
                block.setTransformation(new Transformation(new Vector3f(-0.5f, -0.04375f, -0.5f), new AxisAngle4f(0, 0, 0, 0), new Vector3f(1f, 1f, 1f), new AxisAngle4f(0, 0, 0, 0)));
                block.setBlock(b.getBlockData());

                hitbox.addPassenger(block);
                p.addPassenger(base);
            }

            if(!config.getBoolean("useCustomSounds")) {
                p.playSound(p, b.getBlockData().getSoundGroup().getBreakSound(), 5, 0.8f);
            } else {
                p.playSound(p, "defenestrate.pickup", 5, 1);
            }


            b.setType(Material.AIR);
            e.setCancelled(true);
        }



        // Throwing
        else if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK){
            Throw(p, passenger);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player p) {
            Entity passenger = plugin.getPassenger(p);
            if(passenger != null && e.getEntity() == passenger) {
                Throw(p, passenger);
            }
        }
    }

    public boolean inSpawnProt(Location location) {
        if(plugin.getConfig().getBoolean("ignoreSpawnProt")) { return false;}

        World world = location.getWorld();

        Location spawnLocation = world.getSpawnLocation();
        int spawnRadius = Bukkit.getServer().getSpawnRadius();

        if (!world.getEnvironment().equals(World.Environment.NORMAL)) { return false; }

        // distnce
        int distanceX = Math.abs(location.getBlockX() - spawnLocation.getBlockX());
        int distanceZ = Math.abs(location.getBlockZ() - spawnLocation.getBlockZ());

        return (distanceX <= spawnRadius) && (distanceZ <= spawnRadius);
    }

    public void Throw(Player p, Entity passenger) {

        p.removePassenger(passenger);
        passenger.teleport(p.getLocation().add(0, 2, 0));

        float power;
        // if it's a Player, get player power
        if(passenger instanceof Player) {
            power = (float) config.getDouble("playerThrowPower");
        }

        // if it's a FallingBlock, get block power
        else if (passenger instanceof FallingBlock) {
            power = (float) config.getDouble("blockThrowPower");
        }

        // if it's my custom block, get block power and start the checking loop
        else if(plugin.blocks().contains(passenger.getUniqueId())) {
            new BlockEntityManager((Zoglin) passenger); // This creates a new instance of BlockEntityManager, which is the stupid little class I made to run the loop
            power = (float) config.getDouble("blockThrowPower"); // use block settings
        }

        // otherwise just use the entity power
        else {
            power = (float) config.getDouble("entityThrowPower");
        }

        Vector direction = p.getLocation().getDirection();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> passenger.setVelocity(direction.multiply(power)), 3L);

        if(plugin.getConfig().getBoolean("useCustomSounds")) {
            p.getWorld().playSound(p.getLocation(), "defenestrate.throw", 1, 1f);
        }
    }
}
