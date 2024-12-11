package ru.shirk.fights.menus;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SpecialHolder(UUID uuid) implements InventoryHolder {
    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }
}
