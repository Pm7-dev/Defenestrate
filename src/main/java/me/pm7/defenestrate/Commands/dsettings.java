package me.pm7.defenestrate.Commands;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class dsettings extends SettingsManager implements CommandExecutor, TabExecutor {
    private final Defenestrate plugin = Defenestrate.getPlugin();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Make sure it is a player that is running the command
        if(!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "This command can not be used from the console.");
            return true;
        }

        // Check for permissions
        if(!p.hasPermission("defenestrate.settings") && !p.hasPermission("defenestrate.all")) {
            p.sendMessage(ChatColor.RED + "Insufficient permissions.");
            return true;
        }

        // Quick argument check
        if(args.length == 0) {
            p.sendMessage(ChatColor.RED + "This command requires arguments. Use \"/help dsettings\" for more info");
            return true;
        }

        // Getting the value of a setting
        if(args[0].equalsIgnoreCase("get")) {

            // Argument check again
            if(args.length < 2) {
                p.sendMessage(ChatColor.RED + "Please specify the setting you would like to get");
                return true;
            }

            // Check to make sure the specified setting actually exists
            String setting = getProperCase(args[1]);
            if(config.get(setting) == null) {
                p.sendMessage(ChatColor.RED + "That setting doesn't exist!");
                return true;
            }

            if(booleanSettings().contains(setting)) {
                if(config.getBoolean(setting)) {
                    p.sendMessage(ChatColor.YELLOW + setting + " is currently set to " + ChatColor.GREEN + "" + ChatColor.BOLD + config.get(setting));
                } else {
                    p.sendMessage(ChatColor.YELLOW + setting + " is currently set to " + ChatColor.RED + "" + ChatColor.BOLD + config.get(setting));
                }
            }
            else if(floatSettings().contains(setting)) {
                p.sendMessage(ChatColor.YELLOW + setting + " is currently set to " + ChatColor.BLUE + "" + ChatColor.BOLD + config.get(setting));
            }

            return true;

        }

        // Setting the value of a setting
        if (args[0].equalsIgnoreCase("set")) {
            if(args.length < 3) {
                p.sendMessage(ChatColor.RED + "Please specify both a setting and the value you would like to set it to.");
                return true;
            }

            // Check to make sure the specified setting actually exists
            String setting = getProperCase(args[1]);
            if(config.get(setting) == null) {
                p.sendMessage(ChatColor.RED + "That setting doesn't exist!");
                return true;
            }

            // Figure out what kind of data we are dealing with
            if(booleanSettings().contains(setting)) {

                // Make sure the response is a boolean
                if(!args[2].equalsIgnoreCase("false") && !args[2].equalsIgnoreCase("true")) {
                    p.sendMessage(ChatColor.RED + "This value must either be set to \"true\" or \"false\"");
                    return true;
                }

                Boolean newValue = Boolean.valueOf(args[2]);
                if(newValue) {
                    p.sendMessage(ChatColor.YELLOW + "Changing " + setting + " to " + ChatColor.GREEN + "" + ChatColor.BOLD + newValue);
                } else {
                    p.sendMessage(ChatColor.YELLOW + "Changing " + setting + " to " + ChatColor.RED + "" + ChatColor.BOLD + newValue);
                }
                config.set(setting, newValue);
                plugin.saveConfig();

                return true;
            } else if (floatSettings().contains(setting)) {

                // Make sure the response is a float
                float newValue;
                try { newValue = Float.parseFloat(args[2]);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "This value must be set to a float value (number with decimal)");
                    return true;
                }
                p.sendMessage(ChatColor.YELLOW + "Changing " + setting + " to " + ChatColor.BLUE + "" + ChatColor.BOLD + newValue);
                config.set(setting, newValue);
                plugin.saveConfig();

                return true;
            }
        }

        // Listing all the settings
        if (args[0].equalsIgnoreCase("list")) {
            int i = 0;
            p.sendMessage(ChatColor.GOLD + "--------------- DEFENESTRATE SETTINGS ---------------");
            p.sendMessage("");
            p.sendMessage(ChatColor.GOLD + "Power Settings:");
            for(String key : config.getKeys(false)) {

                if(i==3) {
                    p.sendMessage("");
                    p.sendMessage(ChatColor.GOLD + "Feature Settings:");
                } else if (i==6) {
                    p.sendMessage("");
                    p.sendMessage(ChatColor.GOLD + "Permission Settings:");
                } else if (i==9) {
                    p.sendMessage("");
                    p.sendMessage(ChatColor.GOLD + "Other Settings:");
                }

                if(booleanSettings().contains(key)) {
                    if(config.getBoolean(key)) {
                        p.sendMessage(ChatColor.YELLOW + key + ": " + ChatColor.GREEN + "" + ChatColor.BOLD + config.get(key));
                    } else {
                        p.sendMessage(ChatColor.YELLOW + key + ": " + ChatColor.RED + "" + ChatColor.BOLD + config.get(key));
                    }
                }
                else if(floatSettings().contains(key)) {
                    p.sendMessage(ChatColor.YELLOW + key + ": " + ChatColor.BLUE + "" + ChatColor.BOLD + config.get(key));
                }
                i++;
            }
            p.sendMessage("");
            p.sendMessage(ChatColor.YELLOW + "Note: entity/block blacklists are not listed for chat flood reasons. Please use \"/dsettings blacklist [listBlocks|listEntities]\" if you want to view those.");
            p.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");

            return true;
        }

        // Block/Entity blacklist management
        if(args[0].equalsIgnoreCase("blacklist")) {
            switch (args[1].toLowerCase()) {
                case "addblock" -> {
                    Material mat;
                    try { mat = Material.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        p.sendMessage(ChatColor.RED + "This value must be set to the name of a material");
                        return true;
                    }

                    if(allowedBlocks().contains(mat.toString())) {
                        blockBlock(mat);
                        p.sendMessage(ChatColor.GREEN + "Success!");
                    } else {
                        p.sendMessage(ChatColor.RED + "This block is already blocked!");
                    }
                }
                case "addentity" -> {
                    EntityType et;
                    try { et = EntityType.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        p.sendMessage(ChatColor.RED + "This value must be set to a type of entity");
                        return true;
                    }

                    if(allowedEntities().contains(et.toString())) {
                        blockEntity(et);
                        p.sendMessage(ChatColor.GREEN + "Success!");
                    } else {
                        p.sendMessage(ChatColor.RED + "This entity is already blocked!");
                    }
                }
                case "removeblock" -> {
                    Material mat;
                    try { mat = Material.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        p.sendMessage(ChatColor.RED + "This value must be set to the name of a block");
                        return true;
                    }

                    if(blockedBlocks().contains(mat.toString())) {
                        unblockBlock(mat);
                        p.sendMessage(ChatColor.GREEN + "Success!");
                    } else {
                        p.sendMessage(ChatColor.RED + "This block isn't blocked!");
                    }
                }
                case "removeentity" -> {
                    EntityType et;
                    try { et = EntityType.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException e) {
                        p.sendMessage(ChatColor.RED + "This value must be set to a type of entity");
                        return true;
                    }

                    if(blockedEntities().contains(et.toString())) {
                        unblockEntity(et);
                        p.sendMessage(ChatColor.GREEN + "Success!");
                    } else {
                        p.sendMessage(ChatColor.RED + "This entity isn't blocked!");
                    }
                }
                case "listblock" -> {
                    p.sendMessage(ChatColor.GOLD + "List of every block on the blacklist:");
                    p.sendMessage(ChatColor.YELLOW + blockedBlocks().toString());
                }
                case "listentity" -> {
                    p.sendMessage(ChatColor.GOLD + "List of every entity on the blacklist:");
                    p.sendMessage(ChatColor.YELLOW + blockedEntities().toString());
                }
                default -> p.sendMessage(ChatColor.RED + "Invalid blacklist argument");
            }
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 1: {
                return getTabComplete(settingsOptions(), args[0]);
            }
            case 2: {
                String arg = args[0].toLowerCase();
                if(arg.equals("get") || arg.equals("set")) { return getTabComplete(settingsList(), args[1]); }
                if(arg.equals("blacklist")) { return getTabComplete(blacklistOptions(), args[1]); }
            }
            case 3: {
                String arg = args[0].toLowerCase();
                if(arg.equals("set") && booleanSettings().contains(args[1])) {
                    return getTabComplete(boolOptions(), args[2]);
                }
                if(arg.equals("blacklist")) {
                    arg = args[1].toLowerCase();
                    switch (arg) {
                        case "addblock" -> { return getTabComplete(allowedBlocks(), args[2]); }
                        case "removeblock" -> { return getTabComplete(blockedBlocks(), args[2]); }
                        case "addentity" -> { return getTabComplete(allowedEntities(), args[2]); }
                        case "removeentity" -> { return getTabComplete(blockedEntities(), args[2]); }
                    }
                }
            }
        }
        return new ArrayList<>();
    }

    String getProperCase(String string) {
        for(String setting : settingsList()) {
            if(setting.equalsIgnoreCase(string)) {
                return setting;
            }
        }
        return "error";
    }

    List<String> getTabComplete(List<String> list, String arg) {
        arg = arg.toLowerCase();
        if(arg.isEmpty()) { return list; }

        List<String> availableArgs = new ArrayList<>();
        List<String> argsThatStartWithTerm = new ArrayList<>();
        List<String> argsThatContainTerm = new ArrayList<>();
        for(String setting : list) {
            if(setting.toLowerCase().startsWith(arg)) { argsThatStartWithTerm.add(setting); }
            else if(setting.toLowerCase().contains(arg)) { argsThatContainTerm.add(setting); }
        }

        availableArgs.addAll(argsThatStartWithTerm);
        availableArgs.addAll(argsThatContainTerm);
        return availableArgs;
    }
}
