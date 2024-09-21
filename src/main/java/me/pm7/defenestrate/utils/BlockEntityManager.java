package me.pm7.defenestrate.utils;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Zoglin;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class BlockEntityManager {

    private static final Defenestrate plugin = Defenestrate.getPlugin();

    private static final List<Material> waterlogged = Arrays.asList(
            Material.SEAGRASS,
            Material.TALL_SEAGRASS,
            Material.SEA_PICKLE,
            Material.KELP,
            Material.KELP_PLANT,
            Material.LILY_PAD,
            Material.WATER,
            Material.WATER_CAULDRON
    );

    private final BukkitTask task;



    public BlockEntityManager(Zoglin zoglin) {

        // Repeating task that loops every few ticks to check if the block is done moving
        this.task = new BukkitRunnable() {
            @Override
            public void run() {

                // If the zoglin has been picked up again, cancel the loop
                if(zoglin.isInsideVehicle()) {
                    cancelTask();
                    return;
                }

                // If the zoglin is dead, something has gone quite wrong, so it's best to just clean up so no leftover entities are present
                if(zoglin.isDead()) {
                    plugin.unregisterBlock(zoglin.getUniqueId());

                    if(!zoglin.getPassengers().isEmpty()) {
                        Interaction in = (Interaction) zoglin.getPassengers().getFirst();
                        if(!in.getPassengers().isEmpty()) {
                            BlockDisplay bd = (BlockDisplay) in.getPassengers().getFirst();
                            bd.remove();
                        }
                        in.remove();
                    }
                    zoglin.remove();
                    cancelTask();
                    return;
                }

                // Getting the mob stack and removing if an expected passenger can't be found
                if(zoglin.getPassengers().isEmpty()) {
                    plugin.unregisterBlock(zoglin.getUniqueId());

                    zoglin.remove();
                    cancelTask();
                    return;
                }
                Interaction in = (Interaction) zoglin.getPassengers().getFirst();
                if(in.getPassengers().isEmpty()) {
                    plugin.unregisterBlock(zoglin.getUniqueId());

                    in.remove();
                    zoglin.remove();;
                    cancelTask();
                    return;
                }
                BlockDisplay bd = (BlockDisplay) in.getPassengers().getFirst();


                // If the entity has the same momentum as a still entity in its environment, it's time to drop the block
                Location loc = zoglin.getLocation();
                Vector v = zoglin.getVelocity();

                if(v.equals(new Vector(0,-0.0784000015258789,0))) { // that big -0.07 number is the gravitational constant I guess
                    dropBlock(loc, zoglin, in, bd);
                } else if((isWatered(loc.getBlock().getBlockData()) && v.equals(new Vector(0, -0.005, 0)))) { // much nicer number for gravity in water
                    dropBlock(loc, zoglin, in, bd);
                } else if(loc.getBlock().getType() == Material.LAVA && v.equals(new Vector(0, -0.02, 0))) { // much nicer number for gravity in lava
                    dropBlock(loc, zoglin, in, bd);
                }

                // If the entity falls into the void, kill it and its passengers
                if(zoglin.getLocation().getY() < -65.0d) {
                    plugin.unregisterBlock(zoglin.getUniqueId());

                    bd.remove();
                    in.remove();
                    zoglin.remove();
                    cancelTask();
                }
            }
        }.runTaskTimer(plugin, 10L, 5L);


        long despawnTicks = (long) (plugin.getConfig().getDouble("blockDespawnMinutes") * 1200);
        if(despawnTicks == 0) { return; }

        // 5 minute task to determine if we should just drop the block
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if(zoglin.isDead() || !plugin.blocks().contains(zoglin.getUniqueId())) {
                plugin.unregisterBlock(zoglin.getUniqueId());
                cancelTask();
                return;
            }

            if(zoglin.getPassengers().isEmpty()) {
                plugin.unregisterBlock(zoglin.getUniqueId());
                zoglin.remove();
                cancelTask();
                return;
            }

            Interaction in = (Interaction) zoglin.getPassengers().getFirst();
            if(in.getPassengers().isEmpty()) {
                plugin.unregisterBlock(zoglin.getUniqueId());
                in.remove();
                zoglin.remove();;
                cancelTask();
                return;
            }
            BlockDisplay bd = (BlockDisplay) in.getPassengers().getFirst();

            Location loc = zoglin.getLocation();
            loc.getWorld().dropItemNaturally(loc.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(bd.getBlock().getMaterial()));

            plugin.unregisterBlock(zoglin.getUniqueId());
            bd.remove();
            in.remove();
            zoglin.remove();
            cancelTask();
        }, despawnTicks);
    }

    public void cancelTask() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
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

    boolean isWatered(BlockData block) {
        return Tag.CORAL_PLANTS.isTagged(block.getMaterial()) || waterlogged.contains(block.getMaterial()) || (block instanceof Waterlogged && ((Waterlogged) block).isWaterlogged());
    }





    void dropBlock(Location loc, Zoglin zoglin, Interaction in, BlockDisplay bd) {
        Block block = zoglin.getLocation().getBlock();


        if(!plugin.getConfig().getBoolean("useCustomSounds")) { block.getWorld().playSound(block.getLocation(), bd.getBlock().getSoundGroup().getPlaceSound(), 1, 0.8f); }
        else { block.getWorld().playSound(block.getLocation(), "defenestrate.place", 1, 1f); }

        if(block.getType().isAir() || Tag.REPLACEABLE.isTagged(block.getType()) || !block.getType().isSolid()) {

            // If it is in spawn protection, drop the block item
            if(inSpawnProt(block.getLocation())) {
                if(bd.getBlock().getMaterial().isItem()) {
                    loc.getWorld().dropItemNaturally(loc.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(bd.getBlock().getMaterial()));

                    // container stuff
                    HashMap<UUID, Inventory> invList = plugin.getInvList();
                    if(invList.containsKey(zoglin.getUniqueId())) {
                        Inventory inv = invList.get(zoglin.getUniqueId());
                        World world = loc.getWorld();
                        double power = 0.2D;
                        for(ItemStack item : inv.getContents()) {
                            if(item != null && item.getItemMeta() != null) {
                                if(item.getType() == Material.SPLASH_POTION && item.getItemMeta().getItemName().equals("Orb of Pondering")) {
                                    continue;
                                }
                                double xVel = -power + (Math.random() * (power*2));
                                double zVel = -power + (Math.random() * (power*2));
                                Entity dropped = world.dropItem(loc, item);
                                dropped.setVelocity(new Vector(xVel, 0.3, zVel));
                            }
                        }
                        invList.remove(zoglin.getUniqueId());
                    }
                }
            }

            // If it is not in spawn protection, set the block
            else {

                // if the block is being placed in the nether, make sure it's not water things
                if(block.getWorld().getEnvironment() == World.Environment.NETHER && isWatered(block.getBlockData())) {
                    loc.getWorld().dropItemNaturally(loc.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(bd.getBlock().getMaterial()));

                    // container stuff
                    HashMap<UUID, Inventory> invList = plugin.getInvList();
                    if(invList.containsKey(zoglin.getUniqueId())) {
                        Inventory inv = invList.get(zoglin.getUniqueId());
                        World world = loc.getWorld();
                        double power = 0.2D;
                        for(ItemStack item : inv.getContents()) {
                            if(item != null && item.getItemMeta() != null) {
                                if(item.getType() == Material.SPLASH_POTION && item.getItemMeta().getItemName().equals("Orb of Pondering")) {
                                    continue;
                                }
                                double xVel = -power + (Math.random() * (power*2));
                                double zVel = -power + (Math.random() * (power*2));
                                Entity dropped = world.dropItem(loc, item);
                                dropped.setVelocity(new Vector(xVel, 0.3, zVel));
                            }
                        }
                        invList.remove(zoglin.getUniqueId());
                    }
                }

                else {
                    block.setBlockData(bd.getBlock());

                    HashMap<UUID, Inventory> invList = plugin.getInvList();
                    if(invList.containsKey(zoglin.getUniqueId())) {
                        Container c = (Container) block.getState();
                        c.getInventory().setContents(invList.get(zoglin.getUniqueId()).getContents());
                        invList.remove(zoglin.getUniqueId());
                    }
                }
            }
        }

        // If the block that the zoglin is in is not air, drop the item
        else {
            if(bd.getBlock().getMaterial().isItem()) {
                loc.getWorld().dropItemNaturally(loc.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(bd.getBlock().getMaterial()));

                // container stuff
                HashMap<UUID, Inventory> invList = plugin.getInvList();
                if(invList.containsKey(zoglin.getUniqueId())) {
                    Inventory inv = invList.get(zoglin.getUniqueId());
                    World world = loc.getWorld();
                    double power = 0.2D;
                    for(ItemStack item : inv.getContents()) {
                        if(item != null && item.getItemMeta() != null) {
                            if(item.getType() == Material.SPLASH_POTION && item.getItemMeta().getItemName().equals("Orb of Pondering")) {
                                continue;
                            }
                            double xVel = -power + (Math.random() * (power*2));
                            double zVel = -power + (Math.random() * (power*2));
                            Entity dropped = world.dropItem(loc, item);
                            dropped.setVelocity(new Vector(xVel, 0.3, zVel));
                        }
                    }
                    invList.remove(zoglin.getUniqueId());
                }
            }
        }

        plugin.unregisterBlock(zoglin.getUniqueId());

        bd.remove();
        in.remove();
        zoglin.remove();
        cancelTask();
    }
}
