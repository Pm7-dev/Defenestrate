package me.pm7.defenestrate.utils;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Zoglin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class CustomBlock {
    private final Defenestrate plugin = Defenestrate.getPlugin();

    private final Zoglin z;
    private final Interaction i;
    private final BlockDisplay bd;
    private final Object b;

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

    public CustomBlock(Zoglin z, Interaction i, BlockDisplay bd, Object b) {
        this.z = z;
        this.i = i;
        this.bd = bd;
        this.b = b;
    }

    public CustomBlock(Zoglin z, Interaction i, BlockDisplay bd) {
        this.z = z;
        this.i = i;
        this.bd = bd;
        this.b = null;
    }

    public void launch() {
        new BukkitRunnable() {
            @Override
            public void run() {

                // If the zoglin has been picked up again, cancel the loop
                if(z.isInsideVehicle()) {
                    this.cancel();
                    return;
                }

                // If the zoglin is dead, something has gone quite wrong, so it's best to just clean up so no leftover entities are present
                if(z.isDead()) {
                    plugin.unregisterBlock(z.getUniqueId());

                    if(!z.getPassengers().isEmpty()) {
                        Interaction in = (Interaction) z.getPassengers().getFirst();
                        if(!in.getPassengers().isEmpty()) {
                            BlockDisplay bd = (BlockDisplay) in.getPassengers().getFirst();
                            bd.remove();
                        }
                        in.remove();
                    }
                    z.remove();
                    this.cancel();
                    return;
                }

                // Getting the mob stack and removing if an expected passenger can't be found
                if(z.getPassengers().isEmpty()) {
                    plugin.unregisterBlock(z.getUniqueId());

                    z.remove();
                    this.cancel();
                    return;
                }
                Interaction in = (Interaction) z.getPassengers().getFirst();
                if(in.getPassengers().isEmpty()) {
                    plugin.unregisterBlock(z.getUniqueId());

                    in.remove();
                    z.remove();
                    this.cancel();
                    return;
                }
                BlockDisplay bd = (BlockDisplay) in.getPassengers().getFirst();


                // If the entity has the same momentum as a still entity in its environment, it's time to drop the block
                Location loc = z.getLocation();
                Vector v = z.getVelocity();

                if(v.equals(new Vector(0,-0.0784000015258789,0))) { // that big -0.07 number is the gravitational constant I guess
                    dropBlock(loc);
                    this.cancel();
                } else if((isWatered(loc.getBlock().getBlockData()) && v.equals(new Vector(0, -0.005, 0)))) { // much nicer number for gravity in water
                    dropBlock(loc);
                    this.cancel();
                } else if(loc.getBlock().getType() == Material.LAVA && v.equals(new Vector(0, -0.02, 0))) { // much nicer number for gravity in lava
                    dropBlock(loc);
                    this.cancel();
                }

                // If the entity falls into the void, kill it and its passengers
                if(z.getLocation().getY() < -65.0d) {
                    plugin.unregisterBlock(z.getUniqueId());

                    bd.remove();
                    in.remove();
                    z.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 10L, 5L);
    }

    void dropBlock(Location loc) {
        Block block = z.getLocation().getBlock();

        if(!plugin.getConfig().getBoolean("useCustomSounds")) {
            block.getWorld().playSound(block.getLocation(), bd.getBlock().getSoundGroup().getPlaceSound(), 1, 0.8f);
        } else {
            block.getWorld().playSound(block.getLocation(), "defenestrate.place", 1, 1f);
        }

        if(block.getType().isAir() || Tag.REPLACEABLE.isTagged(block.getType()) || !block.getType().isSolid()) {

            // If it is in spawn protection, drop the block item
            if(inSpawnProt(block.getLocation())) {
                if(bd.getBlock().getMaterial().isItem()) {
                    loc.getWorld().dropItemNaturally(loc.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(bd.getBlock().getMaterial()));
                }
            }

            // If it is not in spawn protection, set the block
            else {

                // if the block is being placed in the nether, make sure it's not water things
                if(block.getWorld().getEnvironment() == World.Environment.NETHER && isWatered(block.getBlockData())) {
                    loc.getWorld().dropItemNaturally(loc.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(bd.getBlock().getMaterial()));
                }

                else {
                    block.setBlockData(bd.getBlock());
                }
            }
        }

        // If the block that the zoglin is in is not air, drop the item
        else {
            if(bd.getBlock().getMaterial().isItem()) {
                loc.getWorld().dropItemNaturally(loc.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(bd.getBlock().getMaterial()));
            }
        }

        plugin.unregisterBlock(z.getUniqueId());

        bd.remove();
        i.remove();
        z.remove();
    }

    boolean isWatered(BlockData block) {
        return Tag.CORAL_PLANTS.isTagged(block.getMaterial()) || waterlogged.contains(block.getMaterial()) || (block instanceof Waterlogged && ((Waterlogged) block).isWaterlogged());
    }

    boolean inSpawnProt(Location location) {
        if(plugin.getConfig().getBoolean("ignoreSpawnProt")) { return false; }

        World world = location.getWorld();

        Location spawnLocation = world.getSpawnLocation();
        int spawnRadius = Bukkit.getServer().getSpawnRadius();

        if (!world.getEnvironment().equals(World.Environment.NORMAL)) { return false; }

        // distnce
        int distanceX = Math.abs(location.getBlockX() - spawnLocation.getBlockX());
        int distanceZ = Math.abs(location.getBlockZ() - spawnLocation.getBlockZ());

        return (distanceX <= spawnRadius) && (distanceZ <= spawnRadius);
    }

    void checkBlockType(boolean dropItems) {
        if(b instanceof Container) {
            if(b instanceof Chest) {

            } else if (b instanceof Furnace) {

            } else if (b instanceof Dispenser) {

            }
        }
    }
}
