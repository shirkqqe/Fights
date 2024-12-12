package ru.shirk.fights.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.shirk.fights.Fights;
import ru.shirk.fights.storages.database.DatabaseStorage;
import ru.shirk.fights.storages.redis.RedisManager;
import ru.shirk.fights.tools.Utils;

@AllArgsConstructor
public class QueueManager implements Listener {

    private final DatabaseStorage databaseStorage;
    private final RedisManager redisManager;

    public void handleEnter(@NonNull Player player) {
        if (!Utils.hasArmor(player)) {
            player.sendMessage(Fights.getConfigurationManager().getConfig("settings.yml")
                    .c("messages.enterQueueError"));
            return;
        }
        databaseStorage.addToQueue(player.getName(), Fights.getCurrentServer());
        redisManager.sendMessage(Fights.getCurrentServer(), Fights.getConfigurationManager()
                .getConfig("settings.yml").c("messages.enterQueue")
                .replace("%sender%", player.getName())
                .replace("%server%", Fights.getCurrentServer())
        );
    }

    public QueueUser getQueueUser(@NonNull String name) {
        return databaseStorage.getUser(name);
    }

    public boolean isInQueue(@NonNull String name) {
        return databaseStorage.isInQueue(name);
    }

    public void handleQuit(@NonNull String name) {
        databaseStorage.removeFromQueue(name);
    }

    @EventHandler
    private void onPlayerQuit(@NonNull PlayerQuitEvent event) {
        if (!isInQueue(event.getPlayer().getName())) return;
        handleQuit(event.getPlayer().getName());
    }
}
