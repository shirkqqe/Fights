package ru.shirk.fights.menus;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import ru.shirk.fights.Fights;
import ru.shirk.fights.queue.QueueUser;
import ru.shirk.fights.tools.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class QueueListMenu implements Listener {

    private final Map<UUID, InventoryData> viewing = new HashMap<>();
    private final ItemStack decor = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
    private final ItemStack decor2 = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
    private final ItemStack prevPage = new ItemStack(Material.ARROW);
    private final ItemStack nextPage = new ItemStack(Material.ARROW);

    {
        ItemMeta itemMeta = prevPage.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(ChatColor.WHITE + "<- Предыдущая страница");
        prevPage.setItemMeta(itemMeta);

        itemMeta = nextPage.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(ChatColor.WHITE + "Следующая страница ->");
        nextPage.setItemMeta(itemMeta);

        itemMeta = decor.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(" ");
        decor.setItemMeta(itemMeta);

        itemMeta = decor2.getItemMeta();
        assert itemMeta != null;
        itemMeta.setDisplayName(" ");
        decor2.setItemMeta(itemMeta);
    }

    public QueueListMenu(final JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::updateAll, 20, 20);
    }

    public void open(final Player player) {
        player.closeInventory();

        SpecialHolder holder = new SpecialHolder(player.getUniqueId());
        final Inventory inventory = Bukkit.createInventory(holder, 54, ChatColor.DARK_GRAY + "Игроки, ожидающие схватку");
        InventoryData inventoryData = new InventoryData(inventory, 0);
        viewing.put(player.getUniqueId(), inventoryData);
        build(player, inventory);
        player.openInventory(inventory);
    }

    public void updateAll() {
        for (final Map.Entry<UUID, InventoryData> entry : viewing.entrySet()) {
            final Player p = Bukkit.getPlayer(entry.getKey());
            if (p == null) continue;
            update(p);
        }
    }

    public void update(final Player player) {
        final InventoryData inventoryData = viewing.get(player.getUniqueId());
        if (inventoryData == null) return;

        Inventory inventory = inventoryData.getInventory();
        if (!player.getOpenInventory().getTopInventory().equals(inventory)) {
            return;
        }

        build(player, inventory);
    }

    public void build(final Player player, final Inventory inventory) {
        InventoryData inventoryData = viewing.get(player.getUniqueId());
        if (inventoryData == null) return;

        int slot = 9;
        int page = inventoryData.getPage();
        int startIndex = page * 36;

        inventory.clear();

        final QueueUser[] queueUsers = Fights.getQueueManager().getQueue().toArray(new QueueUser[0]);
        for (int i = startIndex; i < queueUsers.length && slot < 44; i++) {
            final QueueUser queueUser = queueUsers[i];
            inventory.setItem(slot, newQueueUserItem(queueUser));
            slot++;
        }

        inventory.setItem(0, decor);
        inventory.setItem(1, decor);
        inventory.setItem(2, decor);
        inventory.setItem(3, decor2);
        inventory.setItem(4, decor2);
        inventory.setItem(5, decor2);
        inventory.setItem(6, decor);
        inventory.setItem(7, decor);
        inventory.setItem(8, decor);
        inventory.setItem(45, decor);
        inventory.setItem(46, decor);
        inventory.setItem(47, decor);
        inventory.setItem(49, decor2);
        inventory.setItem(51, decor);
        inventory.setItem(52, decor);
        inventory.setItem(53, decor);

        if (startIndex + 36 < queueUsers.length) {
            inventory.setItem(50, nextPage);
        } else {
            inventory.setItem(50, decor2);
        }

        if (page > 0) {
            inventory.setItem(48, prevPage);
        } else {
            inventory.setItem(48, decor2);
        }

        player.updateInventory();
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        InventoryData inventoryData = viewing.get(p.getUniqueId());
        Inventory clickedInventory = event.getInventory();
        if (inventoryData == null || !clickedInventory.equals(inventoryData.getInventory())) return;

        event.setCancelled(true);

        if (event.getCurrentItem() != null && event.getCurrentItem().isSimilar(nextPage)) {
            inventoryData.nextPage();
            build(p, inventoryData.getInventory());
        } else if (event.getCurrentItem() != null && event.getCurrentItem().isSimilar(prevPage)) {
            inventoryData.prevPage();
            build(p, inventoryData.getInventory());
        } else {
            final ItemStack itemStack = event.getCurrentItem();
            if (itemStack == null || itemStack.getItemMeta() == null) return;
            final String username = itemStack.getItemMeta().
                    getPersistentDataContainer().get(NamespacedKey.minecraft("username"), PersistentDataType.STRING);
            if (username == null || username.equalsIgnoreCase(p.getName())) return;
            p.closeInventory();
            if (!Utils.hasArmor(p)) {
                p.sendMessage(Fights.getConfigurationManager().getConfig("settings.yml")
                        .c("messages.enterQueueError"));
                return;
            }
            Fights.getQueueManager().handleQuit(username);
            event.getInventory().removeItem(itemStack);
            Bukkit.getScheduler().runTaskAsynchronously(Fights.getInstance(), () ->
                    Fights.getBattleManager().startBattle(Bukkit.getPlayer(username), p));
        }
    }

    @EventHandler
    public void closeInventoryEvent(InventoryCloseEvent event) {
        final UUID uuid = event.getPlayer().getUniqueId();
        Player player = (Player) event.getPlayer();

        if (player.getOpenInventory().getTopInventory().equals(event.getInventory())) return;
        viewing.remove(uuid);
    }

    @EventHandler
    public void disablePluginEvent(PluginDisableEvent event) {
        for (final Map.Entry<UUID, InventoryData> entry : this.viewing.entrySet()) {
            final UUID uuid = entry.getKey();
            final Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.closeInventory();
            }
            this.viewing.remove(uuid);
        }
    }

    private ItemStack newQueueUserItem(final QueueUser queueUser) {
        final ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        final ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;
        itemMeta.getPersistentDataContainer().set(
                NamespacedKey.minecraft("username"),
                PersistentDataType.STRING,
                queueUser.name()
        );
        itemMeta.setDisplayName(" §6● §fПоединок с: §6" + queueUser.name());
        itemMeta.setLore(List.of(
                " ",
                "§r    §aПобеды",
                "§r    §a§l▎§f Всего: §a" + queueUser.wins(),
                "§r    §4Поражения",
                "§r    §4§l▎§f Всего: §4" + queueUser.losses(),
                " ",
                " §b●§f Поединок состоится на §bКлассик-1",
                " ",
                " §6▶§f Левый клик - §6начать поединок",
                " §b▶§f Шифт + Правый клик - §bинвентарь игрока"
        ));
        SkullMeta skullMeta = (SkullMeta) itemMeta;
        skullMeta.setOwningPlayer(Bukkit.getPlayer(queueUser.name()));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    @Getter
    public static class InventoryData {
        private final Inventory inventory;
        private int page;

        public InventoryData(Inventory inventory, int page) {
            this.inventory = inventory;
            this.page = page;
        }

        public void nextPage() {
            this.page++;
        }

        public void prevPage() {
            if (this.page > 0) this.page--;
        }
    }
}
