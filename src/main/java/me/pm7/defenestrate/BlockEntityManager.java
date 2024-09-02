package me.pm7.defenestrate;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;


public class BlockEntityManager {
    private final Defenestrate plugin = Defenestrate.getPlugin();
    private final BukkitTask task;

    public BlockEntityManager(Axolotl axolotl) {

        this.task = new BukkitRunnable() {
            @Override
            public void run() {

                // If the axolotl has been picked up again, cancel the loop
                if(axolotl.isInsideVehicle()) {
                    cancelTask();
                    return;
                }

                // If the axolotl is dead, something has gone quite wrong, so it's best to just clean up so no leftover entities are present
                if(axolotl.isDead()) {
                    plugin.unregisterBlock(axolotl.getUniqueId());

                    if(!axolotl.getPassengers().isEmpty()) {
                        Interaction in = (Interaction) axolotl.getPassengers().get(0);
                        if(!in.getPassengers().isEmpty()) {
                            BlockDisplay bd = (BlockDisplay) in.getPassengers().get(0);
                            bd.remove();
                        }
                        in.remove();
                    }
                    axolotl.remove();
                    cancelTask();
                    return;
                }

                // Getting the mob stack and removing if an expected passenger can't be found
                if(axolotl.getPassengers().isEmpty()) {
                    plugin.unregisterBlock(axolotl.getUniqueId());

                    axolotl.remove();
                    cancelTask();
                    return;
                }
                Interaction in = (Interaction) axolotl.getPassengers().get(0);
                if(in.getPassengers().isEmpty()) {
                    plugin.unregisterBlock(axolotl.getUniqueId());

                    in.remove();
                    axolotl.remove();;
                    cancelTask();
                    return;
                }
                BlockDisplay bd = (BlockDisplay) in.getPassengers().get(0);



                // If the entity is still in the same spot since last tick, it's time to turn it into a block
                if(axolotl.getVelocity().equals(new Vector(0.0,-0.0784000015258789,0.0))) {
                    Location loc = axolotl.getLocation();
                    Block block = axolotl.getLocation().getBlock();
                    if(block.getType().isAir() || Tag.REPLACEABLE.isTagged(block.getType()) || !block.getType().isSolid()) {

                        // If it is in spawn protection, drop the block item
                        if(inSpawnProt(block.getLocation())) {
                            loc.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(bd.getBlock().getMaterial()));
                        }

                        // If it is not in spawn protection, set the block
                        else {
                            block.setBlockData(bd.getBlock());
                        }
                    }

                    // If the block that the axolotl is in is not air, drop the item
                    else {
                        loc.getWorld().dropItemNaturally(loc.getBlock().getLocation().add(0.5, 0.5, 0.5), new ItemStack(bd.getBlock().getMaterial()));
                    }

                    plugin.unregisterBlock(axolotl.getUniqueId());

                    bd.remove();
                    in.remove();
                    axolotl.remove();
                    cancelTask();
                    return;
                }

                // If the entity falls into the void, kill it and its passengers
                if(axolotl.getLocation().getY() < -65.0d) {
                    plugin.unregisterBlock(axolotl.getUniqueId());

                    bd.remove();
                    in.remove();
                    axolotl.remove();
                    cancelTask();
                    return;
                }
            }
        }.runTaskTimer(plugin, 10L, 3L);
    }

    public void cancelTask() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public boolean inSpawnProt(Location location) {
        World world = location.getWorld();

        Location spawnLocation = world.getSpawnLocation();
        int spawnRadius = Bukkit.getServer().getSpawnRadius();

        if (!world.getEnvironment().equals(World.Environment.NORMAL)) { return false; }

        // distnce
        int distanceX = Math.abs(location.getBlockX() - spawnLocation.getBlockX());
        int distanceZ = Math.abs(location.getBlockZ() - spawnLocation.getBlockZ());

        return (distanceX <= spawnRadius) && (distanceZ <= spawnRadius);
    }
}
