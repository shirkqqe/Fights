package ru.shirk.fights.storages.files;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.shirk.fights.Fights;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Configuration {

    private final JavaPlugin p;
    private FileConfiguration config;
    private File file;
    private final String name;


    public Configuration(Fights plugin, String absolutePath) {
        this.config = null;
        this.file = null;

        this.p = plugin;
        this.name = absolutePath;
    }

    public void reload() {
        if (this.file == null) {
            this.file = new File(this.p.getDataFolder(), this.name);
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
        if (!this.file.exists()) {
            this.file.getParentFile().mkdirs();
            Fights.getConfigurationManager().createFile(this.name);
        }
        YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new File(this.name));
        this.config.setDefaults(defConfig);
    }

    public FileConfiguration getFile() {
        if (this.config == null) {
            reload();
        }
        return this.config;
    }

    public void saveDefaultConfig() {
        if (this.file == null) {
            this.file = new File(this.name);
        }
        if (!this.file.exists()) {
            this.p.saveResource(this.name, false);
        }
    }

    public void save() {
        if (this.config == null || this.file == null) {
            return;
        }
        try {
            getFile().save(this.file);
        } catch (IOException ex) {
            Fights.getInstance().getSLF4JLogger().info("Could not save config to {}", this.file, ex);
        }
    }

    public String c(String name) {
        String caption = getFile().getString(name);
        if (caption == null) {
            this.p.getLogger().warning("No such language caption found: " + name);
            caption = "&c[No language caption found]";
        }
        return ChatColor.translateAlternateColorCodes('&', caption);
    }

    public int ch(String name) {
        return getFile().getInt(name);
    }

    public List<String> cl(String name) {
        List<String> captionlist = new ArrayList<>();
        for (String s : getFile().getStringList(name)) {
            captionlist.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        if (getFile().getStringList(name) == null) {
            this.p.getLogger().warning("No such language caption found: " + name);
            captionlist.add(ChatColor.translateAlternateColorCodes('&', "&c[No language caption found]"));
        }
        return captionlist;
    }

    public Material m(String name) {
        final Material material = Material.getMaterial(c(name));
        if(material == null) {
            Bukkit.getLogger().warning("No such material found: " + name);
            return Material.STONE;
        } else {
            return material;
        }
    }

    public World w(String name) {
        final World world = Bukkit.getWorld(c(name));
        if(world == null) {
            Bukkit.getLogger().warning("No such world found: " + name);
            return null;
        } else {
            return world;
        }
    }

    public List<Material> ml(String name) {
        final List<Material> materials = new ArrayList<>();
        for(String s : cl(name)) {
            Material material = Material.getMaterial(s);
            if(material == null) {
                Bukkit.getLogger().warning("No such material found: " + name);
                continue;
            }
            materials.add(material);
        }
        return materials;
    }
}
