package me.pm7.defenestrate.utils;

import me.pm7.defenestrate.Defenestrate;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;
import java.util.function.Consumer;

public class UpdateCheck {

    private final Defenestrate plugin;
    private final int ID;

    public UpdateCheck(Defenestrate plugin, int ID) {
        this.plugin = plugin;
        this.ID = ID;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {

            try (InputStream is = URI.create("https://api.spigotmc.org/legacy/update.php?resource=" + this.ID + "/~").toURL().openStream(); Scanner scanner = new Scanner(is)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException e) {
                plugin.getLogger().info("Defenestrate was unable to check for updates: " + e.getMessage());
            }
        });
    }
}