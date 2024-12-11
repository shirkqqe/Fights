package ru.shirk.fights.tools;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ru.shirk.fights.Fights;

public class CountdownTimer {
    public void startCountdown(final Player sender, final Player player, final Location location1, final Location location2) {
        new BukkitRunnable() {
            int timeLeft = 5;

            @Override
            public void run() {
                if (timeLeft > 0) {
                    String titleColor = switch (timeLeft) {
                        case 5 -> ChatColor.GREEN.toString();
                        case 4 -> ChatColor.YELLOW.toString();
                        case 3 -> ChatColor.GOLD.toString();
                        case 2 -> ChatColor.RED.toString();
                        case 1 -> ChatColor.DARK_RED.toString();
                        default -> ChatColor.WHITE.toString();
                    };

                    player.sendTitle(titleColor + timeLeft, "", 10, 20, 10);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);

                    sender.sendTitle(titleColor + timeLeft, "", 10, 20, 10);
                    sender.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    timeLeft--;
                    return;
                }
                player.teleport(location1);
                sender.teleport(location2);
                final EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(sender, player,
                        EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0);
                event.callEvent();
                sender.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
                player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
                sender.sendMessage(Fights.getConfigurationManager().getConfig("settings.yml")
                        .c("messages.start").replace("%player%", player.getName()));
                player.sendMessage(Fights.getConfigurationManager().getConfig("settings.yml")
                        .c("messages.start").replace("%player%", sender.getName()));
                cancel();
            }
        }.runTaskTimer(Fights.getInstance(), 0L, 20L);
    }
}
