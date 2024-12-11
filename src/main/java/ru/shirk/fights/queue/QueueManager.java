package ru.shirk.fights.queue;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.shirk.fights.Fights;
import ru.shirk.fights.tools.Utils;

import java.util.HashSet;
import java.util.Set;

@Getter
public class QueueManager implements Listener {
    private final Set<QueueUser> queue = new HashSet<>();

    public void handleEnter(final Player player) {
        if(Utils.hasArmor(player)) {
            queue.add(Fights.getDatabaseStorage().getUser(player.getName()));
            Bukkit.broadcastMessage(Fights.getConfigurationManager().getConfig("settings.yml")
                    .c("messages.enterQueue").replace("%sender%", player.getName()));
        } else {
            player.sendMessage(Fights.getConfigurationManager().getConfig("settings.yml")
                    .c("messages.enterQueueError"));
        }
    }

    public QueueUser getQueueUser(final String name) {
        for(QueueUser queueUser : queue) {
            if(queueUser.name().equals(name)) return queueUser;
        }
        return null;
    }

    public void handleQuit(final String name) {
        queue.remove(Fights.getDatabaseStorage().getUser(name));
    }

    @EventHandler
    private void onPlayerQuit(final PlayerQuitEvent event) {
        if(getQueueUser(event.getPlayer().getName()) != null) {
            handleQuit(event.getPlayer().getName());
        }
    }
}
