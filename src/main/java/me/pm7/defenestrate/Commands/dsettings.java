package me.pm7.defenestrate.Commands;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class dsettings implements CommandExecutor, TabExecutor {
    private final Defenestrate plugin = Defenestrate.getPlugin();
    FileConfiguration config = plugin.getConfig();

    final List<String> booleanSettings = Arrays.asList(
            "playerThrowEnabled",
            "entityThrowEnabled",
            "blockThrowEnabled",
            "throwPlayersRequiresPermission",
            "throwEntitiesRequiresPermission",
            "throwBlocksRequiresPermission",
            "oldBlockHandling",
            "breakThingsMode"
    );
    final List<String> floatSettings = Arrays.asList(
            "playerThrowPower",
            "entityThrowPower",
            "blockThrowPower"
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        /*
        if(Objects.equals(args[0], "a")) {
            System.out.println(((Player) sender).getVelocity());
            return true;
        }

         */

        // Make sure it is a player that is running the command
        if(!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can not be used from the console.");
            return true;
        }

        // Check for permissions
        Player p = (Player) sender;
        if(!p.hasPermission("defenestrate.settings") && !p.hasPermission("defenestrate.all")) {
            p.sendMessage(ChatColor.RED + "Insufficient permissions.");
            return true;
        }

        // Quick argument check
        if(args.length == 0) {
            p.sendMessage(ChatColor.RED + "This command requires arguments. Use \"/help dsettings\" for more info");
            return true;
        }

        // Running code for getting the value of a setting
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

            if(booleanSettings.contains(setting)) {
                if(config.getBoolean(setting)) {
                    p.sendMessage(ChatColor.YELLOW + setting + " is currently set to " + ChatColor.GREEN + "" + ChatColor.BOLD + config.get(setting));
                } else {
                    p.sendMessage(ChatColor.YELLOW + setting + " is currently set to " + ChatColor.RED + "" + ChatColor.BOLD + config.get(setting));
                }
            }
            else if(floatSettings.contains(setting)) {
                p.sendMessage(ChatColor.YELLOW + setting + " is currently set to " + ChatColor.BLUE + "" + ChatColor.BOLD + config.get(setting));
            }

            return true;

        }

        // Running code for setting the value of a setting
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
            if(booleanSettings.contains(setting)) {

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
            } else if (floatSettings.contains(setting)) {

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

        // Just listing all the values lol
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

                if(booleanSettings.contains(key)) {
                    if(config.getBoolean(key)) {
                        p.sendMessage(ChatColor.YELLOW + key + ": " + ChatColor.GREEN + "" + ChatColor.BOLD + config.get(key));
                    } else {
                        p.sendMessage(ChatColor.YELLOW + key + ": " + ChatColor.RED + "" + ChatColor.BOLD + config.get(key));
                    }
                }
                else if(floatSettings.contains(key)) {
                    p.sendMessage(ChatColor.YELLOW + key + ": " + ChatColor.BLUE + "" + ChatColor.BOLD + config.get(key));
                }
                i++;
            }
            p.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");

            return true;
        }

        p.sendMessage(ChatColor.RED + "The first argument must be either \"get\", \"set\", or \"list\"");
        return true;
    }

    final List<String> settingsList = Arrays.asList(
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

    // The worst method for tab autocomplete you will ever see :3
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if(args.length == 1) {
            String arg = args[0].toLowerCase();

            if(arg.isEmpty()) {
                return Arrays.asList(
                    "get",
                    "set",
                    "list"
                );
            }

            // This is so stupid lol
            List<String> availableArgs = new ArrayList<>();
            if("get".startsWith(arg)) {availableArgs.add("get");}
            if("set".startsWith(arg)) {availableArgs.add("set");}
            if("list".startsWith(arg)) {availableArgs.add("list");}
            if(!"get".startsWith(arg) && "get".contains(arg)) {availableArgs.add("get");}
            if(!"set".startsWith(arg) && "set".contains(arg)) {availableArgs.add("set");}
            if(!"list".startsWith(arg) && "list".contains(arg)) {availableArgs.add("list");}
            return availableArgs;

        } else if(args.length == 2 && !args[0].equals("list")) {
            String arg = args[1].toLowerCase();
            if(arg.isEmpty()) { return settingsList; }

            List<String> availableArgs = new ArrayList<>();
            List<String> argsThatStartWithTerm = new ArrayList<>();
            List<String> argsThatContainTerm = new ArrayList<>();
            for(String setting : settingsList) {
                if(setting.toLowerCase().startsWith(arg)) { argsThatStartWithTerm.add(setting); }
                else if(setting.toLowerCase().contains(arg)) { argsThatContainTerm.add(setting); }
            }

            availableArgs.addAll(argsThatStartWithTerm);
            availableArgs.addAll(argsThatContainTerm);
            return availableArgs;
        } else if (args.length == 3) {
            if(args[0].equalsIgnoreCase("set") && booleanSettings.contains(args[1])) {
                if(args[2].isEmpty()) {
                    return Arrays.asList(
                        "false",
                        "true"
                    );
                }

                String arg = args[2].toLowerCase();

                // This is so stupid lol
                List<String> availableArgs = new ArrayList<>();
                if("false".startsWith(arg)) {availableArgs.add("false");}
                if("true".startsWith(arg)) {availableArgs.add("true");}
                if(!"false".startsWith(arg) && "false".contains(arg)) {availableArgs.add("false");}
                if(!"true".startsWith(arg) && "true".contains(arg)) {availableArgs.add("true");}
                return availableArgs;
            }
        }
        return new ArrayList<>();
    }

    String getProperCase(String string) {
        for(String setting : settingsList) {
            if(setting.equalsIgnoreCase(string)) {
                return setting;
            }
        }
        return "error";
    }
}
