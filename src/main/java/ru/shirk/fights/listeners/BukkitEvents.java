package ru.shirk.fights.listeners;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.shirk.fights.Fights;
import ru.shirk.fights.battles.Battle;

public class BukkitEvents implements Listener {
    @EventHandler
    private void onDeath(PlayerDeathEvent event) {
        final Battle battle = Fights.getBattleManager().getBattleFrom(event.getEntity());
        if (battle == null) return;
        final Player winner = event.getEntity().equals(battle.getSender()) ? battle.getPlayer() : battle.getSender();
        Fights.getBattleManager().stopBattle(battle, winner);
        Bukkit.broadcastMessage(Fights.getConfigurationManager().getConfig("settings.yml")
                .c("messages.end").replace("%killer%", winner.getName())
                .replace("%player%", event.getEntity().getName()));
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Fights.getDatabaseStorage().createUser(event.getPlayer().getName());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        Fights.getDatabaseStorage().removeFromQueue(event.getPlayer().getName());
    }
}
