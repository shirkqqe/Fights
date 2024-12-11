package ru.shirk.fights.menus;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import ru.shirk.fights.Fights;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class QueueMenu implements Listener {

    private final Map<UUID, Inventory> viewing = new HashMap<>();
    private final ItemStack enterQueue = new ItemStack(Material.LIME_CONCRETE);
    private final ItemStack exitQueue = new ItemStack(Material.RED_CONCRETE);
    private final ItemStack info = new ItemStack(Material.NETHER_STAR);

    {
        ItemMeta itemMeta = enterQueue.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName("§a▶ Поиск поединка");
        itemMeta.setLore(List.of(
                "§a§n▎§f Нажмите, чтобы §aначать поиск",
                "§a▎§f противника для ПВП"
        ));
        enterQueue.setItemMeta(itemMeta);

        itemMeta = exitQueue.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName("§4▶ Отмена ПВП");
        itemMeta.setLore(List.of(
                "§4§n▎§f Нажмите, чтобы §4отменить поиск",
                "§4▎§f противника для ПВП"
        ));
        exitQueue.setItemMeta(itemMeta);

        itemMeta = info.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName("§bПолезная информация");
        itemMeta.setLore(List.of(
                " ",
                "§b§n▎§f Нажав на §aзеленую кнопку, §fвы",
                "§b§n▎§f активируете поиск оппонента,",
                "§b▎§f оповестив об этом весь сервер",
                " ",
                "§b§n▎§4 Красная кнопка §fотменяет",
                "§b▎§f статус поиска",
                " ",
                "§b§n▎§f Когда соперник найдется, вы будуте",
                "§b§n▎§f телепортированы к нему в рудиусе",
                "§b▎§f 30 блоков, о чем оповестят обоих игроков",
                " ",
                "§b§n▎§f Главная задача - победить своего",
                "§b§n▎§f соперника. Наградой послужат все",
                "§b§n▎§f ресурсы проигравшего и знаменательное",
                "§b▎§f поздравление в чате. Приятной битвы!",
                " "
        ));
        info.setItemMeta(itemMeta);
    }

    public QueueMenu(final JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::updateAll, 20, 20);
    }

    public void open(final Player player) {
        player.closeInventory();

        final Inventory inventory = Bukkit.createInventory(player, 27, ChatColor.DARK_GRAY + "Поиск PvP");
        build(player, inventory);
        viewing.put(player.getUniqueId(), inventory);
        player.openInventory(inventory);
    }

    public void update(final Player player) {
        final Inventory inventory = viewing.get(player.getUniqueId());
        if (inventory != null) build(player, inventory);
    }

    public void updateAll() {
        for (final Map.Entry<UUID, Inventory> entry : viewing.entrySet()) {
            final Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null) continue;
            update(p);
        }
    }

    public void build(final Player player, final Inventory inventory) {
        if (Fights.getQueueManager().getQueueUser(player.getName()) == null) {
            inventory.setItem(13, enterQueue);
        } else {
            inventory.setItem(13, exitQueue);
        }
        inventory.setItem(18, info);

        player.updateInventory();
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;

        if (viewing.get(p.getUniqueId()) != event.getInventory()) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) return;
        if (event.getCurrentItem().isSimilar(enterQueue)) {
            p.closeInventory();
            Fights.getQueueManager().handleEnter(p);
        } else if (event.getCurrentItem().isSimilar(exitQueue)) {
            Fights.getQueueManager().handleQuit(p.getName());
            p.sendMessage(Fights.getConfigurationManager().getConfig("settings.yml").c("messages.quitQueue"));
            update(p);
        }
    }

    @EventHandler
    private void onInventoryClose(InventoryCloseEvent event) {
        viewing.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    private void onPluginDisable(PluginDisableEvent event) {
        for (final Map.Entry<UUID, Inventory> entry : this.viewing.entrySet()) {
            final UUID uuid = entry.getKey();
            final Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.closeInventory();
            }
            this.viewing.remove(uuid);
        }
    }
}
