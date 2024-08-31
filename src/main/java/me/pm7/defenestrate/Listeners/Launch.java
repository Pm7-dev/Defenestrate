package me.pm7.defenestrate.Listeners;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;


public class Launch implements Listener {
    private final Defenestrate plugin = Defenestrate.getPlugin();
    FileConfiguration config = plugin.getConfig();

    private final List<EntityType> unthrowableEntities = Arrays.asList(
            EntityType.ITEM,
            EntityType.ITEM_DISPLAY,
            EntityType.ITEM_FRAME,
            EntityType.GLOW_ITEM_FRAME,
            EntityType.AREA_EFFECT_CLOUD,
            EntityType.ARROW,
            EntityType.BREEZE_WIND_CHARGE,
            EntityType.FIREBALL,
            EntityType.FIREWORK_ROCKET,
            EntityType.WITHER_SKULL,
            EntityType.WIND_CHARGE,
            EntityType.TEXT_DISPLAY,
            EntityType.SPECTRAL_ARROW,
            EntityType.SMALL_FIREBALL,
            EntityType.SHULKER_BULLET,
            EntityType.SNOWBALL,
            EntityType.OMINOUS_ITEM_SPAWNER,
            EntityType.PAINTING,
            EntityType.EGG,
            EntityType.ENDER_PEARL,
            EntityType.EVOKER_FANGS,
            EntityType.EXPERIENCE_BOTTLE,
            EntityType.EXPERIENCE_ORB,
            EntityType.FISHING_BOBBER,
              EntityType.INTERACTION, //TODO: get this working
            EntityType.LEASH_KNOT,
            EntityType.LIGHTNING_BOLT,
            EntityType.LLAMA_SPIT,
            EntityType.MARKER,
            EntityType.POTION,
            EntityType.UNKNOWN,
            EntityType.HOPPER_MINECART
    );

    private final List<Material> unthrowableBlocks = Arrays.asList(
            Material.COMMAND_BLOCK,
            Material.CHAIN_COMMAND_BLOCK,
            Material.REPEATING_COMMAND_BLOCK,
            Material.BEDROCK,
            Material.BARRIER,
            Material.AIR,
            Material.CAVE_AIR,
            Material.VOID_AIR,
            Material.END_PORTAL_FRAME,
            Material.NETHER_PORTAL,
            Material.END_GATEWAY,
            Material.END_PORTAL,
            Material.STRUCTURE_BLOCK,
            Material.STRUCTURE_VOID,
            Material.REINFORCED_DEEPSLATE,
            Material.JIGSAW,
            Material.LIGHT,
            Material.TALL_GRASS,
            Material.TALL_SEAGRASS,
            Material.LARGE_FERN,
            Material.SUNFLOWER,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY,
            Material.BIG_DRIPLEAF,
            Material.SMALL_DRIPLEAF,
            Material.PITCHER_PLANT,
            Material.SPAWNER,
            Material.PISTON_HEAD,
            Material.MOVING_PISTON,
            Material.REDSTONE_WIRE,
            Material.WHITE_BED,
            Material.ORANGE_BED,
            Material.MAGENTA_BED,
            Material.LIGHT_BLUE_BED,
            Material.YELLOW_BED,
            Material.LIME_BED,
            Material.PINK_BED,
            Material.GRAY_BED,
            Material.LIGHT_GRAY_BED,
            Material.CYAN_BED,
            Material.PURPLE_BED,
            Material.BLUE_BED,
            Material.BROWN_BED,
            Material.GREEN_BED,
            Material.RED_BED,
            Material.BLACK_BED
    );

    // Picking up Players and Entities
    @EventHandler
    public void PickUpEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();

        // permissions
        if(!(e.getRightClicked() instanceof Player)) {
            if(!config.getBoolean("entityThrowEnabled")) { return; }
            if(config.getBoolean("throwEntitiesRequiresPermission")) {
                if(!p.hasPermission("defenestrate.entities") && !p.hasPermission("defenestrate.all")) {
                    return;
                }
            }
        } else {
            if(!config.getBoolean("playerThrowEnabled")) { return; }
            if(config.getBoolean("throwPlayersRequiresPermission")) {
                if(!p.hasPermission("defenestrate.players") && !p.hasPermission("defenestrate.all")) {
                    return;
                }
            }
        }

        // check if the click is valid
        if(!p.isSneaking() || p.getItemInUse() != null) { return;}

        Entity clicked = e.getRightClicked();
        if(unthrowableEntities.contains(e.getRightClicked().getType())) { return; }

        Entity passenger = plugin.getPassenger(p);
        if(passenger == null) { p.addPassenger(clicked); }

        e.setCancelled(true);
    }

    // Picking up Blocks
    @EventHandler
    public void PickUpBlock(PlayerInteractEvent e) {

        // This is stupid. Why does it run for each hand
        if(e.getHand() == EquipmentSlot.OFF_HAND) return;

        Player p = e.getPlayer();
        Entity passenger = plugin.getPassenger(p);
        Action action = e.getAction();

        // Picking up Blocks
        if(passenger == null) {
            if(action != Action.RIGHT_CLICK_BLOCK) {return;}
            if (!p.isSneaking() || p.getItemInUse() != null) { return;}

            // permissions
            if(!config.getBoolean("blockThrowEnabled")) { return; }
            if(config.getBoolean("throwBlocksRequiresPermission")) {
                if(!p.hasPermission("defenestrate.blocks") && !p.hasPermission("defenestrate.all")) {
                    return;
                }
            }

            Block b = e.getClickedBlock();
            if (b == null) { return; }
            if (!plugin.getConfig().getBoolean("breakThingsMode")) {
                if (unthrowableBlocks.contains(b.getType())) { return; }
                if(b.getState() instanceof Container) { return; }
                if(b.getBlockData() instanceof Door) { return; }
            }
            if (b.getDrops().size() > 1) {
                p.sendMessage(ChatColor.RED + "That Block has items in it!");
                return;
            }

            FallingBlock fallingBlock = p.getWorld().spawnFallingBlock(p.getLocation().add(0, 1.5, 0), b.getBlockData());
            b.setType(Material.AIR);

            p.addPassenger(fallingBlock);

            e.setCancelled(true);
        }



        // Throwing
        else if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK){
            p.removePassenger(passenger);
            passenger.teleport(p.getLocation().add(0, 2, 0));

            // Get the direction and power to throw the object at
            int power;
            if(passenger instanceof Player) { power = config.getInt("playerThrowPower"); }
            else if (passenger instanceof FallingBlock) { power = config.getInt("blockThrowPower"); }
            else { power = config.getInt("entityThrowPower"); }
            Vector direction = p.getLocation().getDirection();

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> passenger.setVelocity(direction.multiply(power)), 1L);
        }
    }
}
