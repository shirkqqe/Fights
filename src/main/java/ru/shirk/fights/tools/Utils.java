package ru.shirk.fights.tools;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class Utils {
    public static boolean hasArmor(final Player player) {
        return player.getInventory().getHelmet() != null && player.getInventory().getBoots() != null &&
                player.getInventory().getChestplate() != null && player.getInventory().getLeggings() != null
                && (player.getEquipment().getHelmet().getType().equals(Material.DIAMOND_HELMET)
                || player.getEquipment().getHelmet().getType().equals(Material.NETHERITE_HELMET))
                && (player.getEquipment().getChestplate().getType().equals(Material.DIAMOND_CHESTPLATE)
                || player.getEquipment().getChestplate().getType().equals(Material.NETHERITE_CHESTPLATE))
                && (player.getEquipment().getLeggings().getType().equals(Material.DIAMOND_LEGGINGS)
                || player.getEquipment().getLeggings().getType().equals(Material.NETHERITE_LEGGINGS))
                && (player.getEquipment().getBoots().getType().equals(Material.DIAMOND_BOOTS)
                || player.getEquipment().getBoots().getType().equals(Material.NETHERITE_BOOTS));
    }
}
