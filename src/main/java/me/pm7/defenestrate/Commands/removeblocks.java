package me.pm7.defenestrate.Commands;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class removeblocks implements CommandExecutor {
    private final Defenestrate plugin = Defenestrate.getPlugin();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        // Check for permissions
        if(!sender.hasPermission("defenestrate.settings") && !sender.hasPermission("defenestrate.all")) {
            sender.sendMessage(ChatColor.RED + "Insufficient permissions.");
            return true;
        }

        // Optional radius provided, remove blocks within distance from sender (sender must be a player)
        if(args.length > 0) {

            // Make sure the sender is a player
            if (!(sender instanceof Player p)) {
                sender.sendMessage(ChatColor.RED + "This command must be run as a player if a radius is specified.");
                return true;
            }

            double radius;
            try { radius = Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "You must provide an integer/decimal value for the radius");
                return true;
            }

            // Make sure the radius is positive
            if(radius <= 0) {
                sender.sendMessage(ChatColor.RED + "Radius must be positive.");
                return true;
            }

            sender.sendMessage(ChatColor.GREEN + "Removing all blocks within a distance of " + radius + "!");
            plugin.killRemainingBlocks(radius, p.getLocation());
        }

        // No radius provided, remove all blocks
        else {
            sender.sendMessage(ChatColor.GREEN + "Removing all blocks!");
            plugin.killRemainingBlocks(-1, null);
        }

        return true;
    }
}
