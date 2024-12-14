package ru.shirk.fights;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.shirk.fights.battles.BattleManager;
import ru.shirk.fights.commands.Commands;
import ru.shirk.fights.listeners.BukkitEvents;
import ru.shirk.fights.menus.QueueListMenu;
import ru.shirk.fights.menus.QueueMenu;
import ru.shirk.fights.queue.QueueManager;
import ru.shirk.fights.storages.database.DatabaseStorage;
import ru.shirk.fights.storages.files.ConfigurationManager;
import ru.shirk.fights.storages.redis.RedisManager;

import java.io.File;
import java.util.Objects;

public final class Fights extends JavaPlugin {

    @Getter
    private static Fights instance;
    @Getter
    private static final ConfigurationManager configurationManager = new ConfigurationManager();
    @Getter
    private static final BattleManager battleManager = new BattleManager();
    @Getter
    private static DatabaseStorage databaseStorage;
    @Getter
    private static QueueManager queueManager;
    @Getter
    private static QueueMenu queueMenu;
    @Getter
    private static QueueListMenu queueListMenu;
    private RedisManager redisManager;

    @Override
    public void onEnable() {
        if(!Bukkit.getPluginManager().isPluginEnabled("AntiRelog")) {
            this.getLogger().severe("Depend AntiRelog is not found!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        instance = this;
        loadConfigs();
        databaseStorage = new DatabaseStorage();
        redisManager = new RedisManager();
        queueManager = new QueueManager(databaseStorage, redisManager);
        Objects.requireNonNull(this.getCommand("pvp")).setExecutor(new Commands());
        Objects.requireNonNull(this.getCommand("pvp")).setTabCompleter(new Commands());
        this.getServer().getPluginManager().registerEvents(new BukkitEvents(), this);
        queueMenu = new QueueMenu(this);
        queueListMenu = new QueueListMenu(this);
    }

    @Override
    public void onDisable() {
        databaseStorage.clearQueue();
        redisManager.unload();
        instance = null;
    }

    private void loadConfigs() {
        try {
            if (!(new File(getDataFolder(), "settings.yml")).exists()) {
                getConfigurationManager().createFile("settings.yml");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static @NonNull String getCurrentServer() {
        return configurationManager.getConfig("settings.yml").c("settings.serverName");
    }
}
