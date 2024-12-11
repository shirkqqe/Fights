package ru.shirk.fights.battles;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import ru.shirk.fights.Fights;

import java.util.ArrayList;
import java.util.List;

public class BattleManager {

    private final List<Battle> battles = new ArrayList<>();

    public void startBattle(final Player sender, final Player player) {
        Battle battle = new Battle(sender, player);
        battles.add(battle);
        battle.start();
    }

    public @Nullable Battle getBattleFrom(final Player member) {
        for (Battle battle : battles) {
            if (battle.getSender().getName().equals(member.getName())
                    || battle.getPlayer().getName().equals(member.getName())) return battle;
        }
        return null;
    }

    public void stopBattle(final Battle battle, final Player winner) {
        battle.stop();
        battles.remove(battle);
        Fights.getDatabaseStorage().addWin(winner.getName());
        Fights.getDatabaseStorage().addLoss(winner.equals(battle.getSender()) ?
                battle.getPlayer().getName() : battle.getSender().getName());
    }
}
