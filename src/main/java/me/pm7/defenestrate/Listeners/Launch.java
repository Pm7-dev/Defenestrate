package me.pm7.defenestrate.Listeners;

import me.pm7.defenestrate.BlockEntityManager;
import me.pm7.defenestrate.Defenestrate;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.data.type.Door;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class Launch implements Listener {
    private final Defenestrate plugin = Defenestrate.getPlugin();
    FileConfiguration config = plugin.getConfig();
    List<UUID> debounceList = new ArrayList<>(); // debounce for picking up blocks, since it ghost creates a left click event when you are looking at air sometimes

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
    public void interactEntityListener(PlayerInteractEntityEvent e) {

        // This is stupid. Why does it run for each hand
        if(e.getHand() == EquipmentSlot.OFF_HAND) return;

        Player p = e.getPlayer();

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
        if(unthrowableEntities.contains(clicked.getType())) { return; }

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
                Entity passenger = plugin.getPassenger(p);
                if (passenger == null) {
                    if(clicked.getVehicle().isInsideVehicle()) {
                        clicked.getVehicle().getVehicle().removePassenger(clicked.getVehicle());
                    }
                    p.addPassenger(clicked.getVehicle());
                }
            }
        } else {

            Entity passenger = plugin.getPassenger(p);
            if (passenger == null) {
                p.addPassenger(clicked);
            }
        }

        e.setCancelled(true);
    }

    @EventHandler
    public void interactListener(PlayerInteractEvent e) {

        // This is stupid. Why does it run for each hand
        if(e.getHand() == EquipmentSlot.OFF_HAND) return;

        // do the debounce which needs to be here for some stupid reason
        UUID uuid = e.getPlayer().getUniqueId();
        if(debounceList.contains(uuid)) return;
        debounceList.add(uuid);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> debounceList.remove(uuid), 3L);

        Player p = e.getPlayer();
        Entity passenger = plugin.getPassenger(p);
        Action action = e.getAction();

        // Picking up Blocks
        if(passenger == null) {
            if(action != Action.RIGHT_CLICK_BLOCK) {return;}
            if (!p.isSneaking() || !p.getInventory().getItemInMainHand().getType().isAir()) { return;}

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

                Axolotl base = (Axolotl) world.spawnEntity(loc, EntityType.AXOLOTL);
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
                block.setRotation(0, 0); // paper servers throw a fit when you don't have this (the block is facing the same direction the player is for some reason)
                block.setTransformation(new Transformation(new Vector3f(-0.5f, 0.0f, -0.5f), new AxisAngle4f(0, 0, 0, 0), new Vector3f(1f, 1f, 1f), new AxisAngle4f(0, 0, 0, 0)));
                block.setBlock(b.getBlockData());

                hitbox.addPassenger(block);
                p.addPassenger(base);
            }

            b.setType(Material.AIR);
            e.setCancelled(true);
        }



        // Throwing
        else if(action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK){
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
                new BlockEntityManager((Axolotl) passenger); // This creates a new instance of BlockEntityManager, which is the stupid little class I made to run the loop
                power = (float) config.getDouble("blockThrowPower"); // use block settings
            }

            // otherwise just use the entity power
            else {
                power = (float) config.getDouble("entityThrowPower");
            }

            Vector direction = p.getLocation().getDirection();
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> passenger.setVelocity(direction.multiply(power)), 3L);
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
