package ru.shirk.fights.battles;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import ru.shirk.fights.Fights;
import ru.shirk.fights.storages.files.Configuration;
import ru.shirk.fights.tools.CountdownTimer;
import ru.shirk.fights.tools.LocationGenerator;

import java.util.concurrent.ExecutionException;

public class Battle {

    @Getter
    private final Player sender;
    @Getter
    private final Player player;
    @Getter
    private final Configuration config = Fights.getConfigurationManager().getConfig("settings.yml");
    private Location lastSenderLocation;
    private Location lastPlayerLocation;
    private int taskId;

    public Battle(Player sender, Player player) {
        this.sender = sender;
        this.player = player;
    }

    public void start() {
        try {
            final Location location = LocationGenerator.generateRandomLocationAsync(
                    config.ch("location.radius"),
                    config.ml("location.blockList"),
                    config.w("location.world")
            ).get();
            if (location == null) {
                Fights.getInstance().getLogger().warning("Не удалось найти локацию для боя.");
                return;
            }

            Location location2 = location.clone().add(location.getDirection().normalize().multiply(-30));
            location2 = location2.getWorld().getHighestBlockAt(location2.getBlockX(), location2.getBlockZ()).getLocation()
                    .add(0, 1, 0);

            location.setDirection(location2.toVector().subtract(location.toVector()));
            location2.setDirection(location.toVector().subtract(location2.toVector()));

            final CountdownTimer countdownTimer = new CountdownTimer();
            countdownTimer.startCountdown(sender, player, location, location2);
            run();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void run() {
        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                if (!sender.isOnline() || !player.isOnline()) {
                    cancel();
                    return;
                }

                double distance = sender.getLocation().distance(player.getLocation());

                if (distance > config.ch("settings.maxDistance")) {
                    Player leaver = determineLeaver();

                    cancel();
                    handleLeave(leaver);
                } else {
                    lastSenderLocation = sender.getLocation();
                    lastPlayerLocation = player.getLocation();
                }
            }
        }.runTaskTimerAsynchronously(Fights.getInstance(), 20, 20).getTaskId();
    }

    private Player determineLeaver() {
        return (lastSenderLocation.distance(sender.getLocation()) >
                lastPlayerLocation.distance(player.getLocation())) ? sender : player;
    }

    @SuppressWarnings("deprecation")
    private void handleLeave(Player leaver) {
        final Player winner = (leaver.equals(sender)) ? player : sender;

        Bukkit.broadcastMessage(config.c("messages.leave")
                .replace("%player%", winner.getName()).replace("%leaver%", leaver.getName()));
        Fights.getBattleManager().stopBattle(this, winner);
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(taskId);
        this.lastSenderLocation = null;
        this.lastPlayerLocation = null;
    }
}

