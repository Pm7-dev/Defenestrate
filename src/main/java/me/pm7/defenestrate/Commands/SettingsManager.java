package me.pm7.defenestrate.Commands;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsManager {
    private static final Defenestrate plugin = Defenestrate.getPlugin();
    static FileConfiguration config = plugin.getConfig();

    private static final List<String> settingsList = Arrays.asList(
            "throwBlocksRequiresPermission",
            "throwEntitiesRequiresPermission",
            "throwPlayersRequiresPermission",
            "blockThrowEnabled",
            "blockThrowPower",
            "entityThrowEnabled",
            "entityThrowPower",
            "playerThrowEnabled",
            "playerThrowPower",
            "oldBlockHandling",
            "breakThingsMode"
    );

    private static final List<String> booleanSettings = Arrays.asList(
            "playerThrowEnabled",
            "entityThrowEnabled",
            "blockThrowEnabled",
            "throwPlayersRequiresPermission",
            "throwEntitiesRequiresPermission",
            "throwBlocksRequiresPermission",
            "oldBlockHandling",
            "breakThingsMode"
    );

    private static final List<String> floatSettings = Arrays.asList(
            "playerThrowPower",
            "entityThrowPower",
            "blockThrowPower"
    );

    private static final List<String> settingsOptions = Arrays.asList(
            "get",
            "set",
            "list",
            "blacklist"
    );

    private static final List<String> blacklistOptions = Arrays.asList(
            "addBlock",
            "addEntity",
            "removeBlock",
            "removeEntity",
            "listBlock",
            "listEntity"
    );

    private static final List<String> boolOptions = Arrays.asList(
            "true",
            "false"
    );

    private static List<String> allowedBlocks = new ArrayList<>();
    private static List<String> allowedEntities = new ArrayList<>();
    private static List<String> blockedBlocks = new ArrayList<>();
    private static List<String> blockedEntities = new ArrayList<>();

    public static void setup() {
        blockedBlocks = config.getStringList("blockedBlocks");
        blockedEntities = config.getStringList("blockedEntities");

        allowedBlocks = new ArrayList<>();
        for(Material mat : Material.values()) {
            if(blockedBlocks.contains(mat.toString())) { continue; }
            allowedBlocks.add(mat.toString());
        }

        allowedEntities = new ArrayList<>();
        for(EntityType et : EntityType.values()) {
            if(blockedEntities.contains(et.toString())) { continue; }
            allowedEntities.add(et.toString());
        }
    }

    public static List<String> settingsList() {return settingsList;}
    public static List<String> booleanSettings() {return booleanSettings;}
    public static List<String> floatSettings() {return floatSettings;}
    public static List<String> settingsOptions() {return settingsOptions;}
    public static List<String> blacklistOptions() {return blacklistOptions;}
    public static List<String> boolOptions() {return boolOptions;}
    public static List<String> allowedBlocks() {return allowedBlocks;}
    public static List<String> allowedEntities() {return allowedEntities;}
    public static List<String> blockedBlocks() {return blockedBlocks;}
    public static List<String> blockedEntities() {return blockedEntities;}

    public void blockEntity(EntityType et) {
        allowedEntities().remove(et.toString());
        if(!blockedEntities().contains(et.toString())) {
            blockedEntities().add(et.toString());
        }
        config.set("blockedEntities", blockedEntities());
        plugin.saveConfig();
    }
    public void blockBlock(Material mat) {
        allowedBlocks().remove(mat.toString());
        if(!blockedBlocks().contains(mat.toString())) {
            blockedBlocks().add(mat.toString());
        }
        config.set("blockedBlocks", blockedBlocks());
        plugin.saveConfig();
    }
    public void unblockEntity(EntityType et) {
        blockedEntities().remove(et.toString());
        if(!allowedEntities().contains(et.toString())) {
            allowedEntities().add(et.toString());
        }
        config.set("blockedEntities", blockedEntities());
        plugin.saveConfig();
    }
    public void unblockBlock(Material mat) {
        blockedBlocks().remove(mat.toString());
        if(!allowedBlocks.contains(mat.toString())) {
            allowedBlocks.add(mat.toString());
        }
        config.set("blockedBlocks", blockedBlocks());
        plugin.saveConfig();
    }
}