package com.minezone.display.gui;

import com.minezone.display.DisplaySystemPlugin;
import com.minezone.display.config.DisplayConfig;
import com.minezone.display.ranking.RankingEntry;
import com.minezone.display.ranking.RankingManager;
import com.minezone.display.ranking.RankingSnapshot;
import com.minezone.display.ranking.RankingType;
import com.minezone.display.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class RankingGUIService implements Listener {
    private static final int PAGE_SIZE = 10;
    private static final int[] FIRST_PAGE_SLOTS = {11, 13, 15, 28, 29, 30, 31, 32, 33, 34};
    private static final int[] REGULAR_SLOTS = {11, 12, 13, 14, 15, 29, 30, 31, 32, 33};

    private final DisplayConfig config;
    private final RankingManager rankings;

    public RankingGUIService(DisplaySystemPlugin plugin, DisplayConfig config, RankingManager rankings) {
        this.config = config;
        this.rankings = rankings;
    }

    public void open(Player player, RankingType type, int requestedPage) {
        if (player == null || type == null) return;
        RankingSnapshot snapshot = rankings.snapshot(type);
        int available = Math.min(snapshot.entries().size(), config.guiLimit(type));
        int pages = Math.max(1, (int) Math.ceil(available / (double) PAGE_SIZE));
        int page = Math.max(0, Math.min(requestedPage, pages - 1));

        RankingGUIHolder holder = new RankingGUIHolder(type, page);
        Inventory inventory = Bukkit.createInventory(holder, 54,
                TextUtil.color("&8" + type.displayName() + " &7• &f" + (page + 1) + "/" + pages));
        holder.bind(inventory);
        decorate(inventory);
        inventory.setItem(4, infoItem(type, snapshot, available, page, pages));

        if (!snapshot.available()) {
            inventory.setItem(22, simple(Material.BARRIER, "&cRanking indisponível",
                    List.of(snapshot.statusMessage().isBlank() ? "&7Tente novamente em instantes." : snapshot.statusMessage())));
        } else if (available == 0) {
            inventory.setItem(22, simple(Material.COBWEB, "&7Nenhum dado no ranking", List.of("&8Ainda não há posições para mostrar.")));
        } else {
            int start = page * PAGE_SIZE;
            int end = Math.min(available, start + PAGE_SIZE);
            int[] slots = page == 0 ? FIRST_PAGE_SLOTS : REGULAR_SLOTS;
            for (int i = start; i < end; i++) {
                RankingEntry entry = snapshot.entries().get(i);
                int local = i - start;
                inventory.setItem(slots[local], rankingItem(type, entry, i + 1));
            }
        }

        if (page > 0) inventory.setItem(45, simple(Material.ARROW, "&a← Página anterior", List.of("&7Ir para a página " + page + ".")));
        else inventory.setItem(45, simple(Material.GRAY_DYE, "&8← Página anterior", List.of("&7Você está na primeira página.")));

        inventory.setItem(49, simple(Material.BARRIER, "&cFechar", List.of("&7Fechar o ranking.")));

        if (page + 1 < pages) inventory.setItem(53, simple(Material.ARROW, "&aPróxima página →", List.of("&7Ir para a página " + (page + 2) + ".")));
        else inventory.setItem(53, simple(Material.GRAY_DYE, "&8Próxima página →", List.of("&7Você está na última página.")));

        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof RankingGUIHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) return;

        switch (event.getRawSlot()) {
            case 45 -> {
                if (holder.page() > 0) open(player, holder.type(), holder.page() - 1);
            }
            case 49 -> player.closeInventory();
            case 53 -> {
                int available = Math.min(rankings.snapshot(holder.type()).entries().size(), config.guiLimit(holder.type()));
                int pages = Math.max(1, (int) Math.ceil(available / (double) PAGE_SIZE));
                if (holder.page() + 1 < pages) open(player, holder.type(), holder.page() + 1);
            }
            default -> { }
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof RankingGUIHolder) event.setCancelled(true);
    }

    private ItemStack rankingItem(RankingType type, RankingEntry entry, int position) {
        ItemStack item;
        if (entry.profileUuid() != null) {
            item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skull = (SkullMeta) item.getItemMeta();
            skull.setOwningPlayer(Bukkit.getOfflinePlayer(entry.profileUuid()));
            applyRankingMeta(skull, entry, position);
            item.setItemMeta(skull);
            return item;
        }

        Material icon = entry.icon() == null || entry.icon().isAir() ? Material.PAPER : entry.icon();
        item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();
        applyRankingMeta(meta, entry, position);
        item.setItemMeta(meta);
        return item;
    }

    private void applyRankingMeta(ItemMeta meta, RankingEntry entry, int position) {
        String prefix = switch (position) {
            case 1 -> "&6🥇 #1 ";
            case 2 -> "&7🥈 #2 ";
            case 3 -> "&c🥉 #3 ";
            default -> "&e#" + position + " ";
        };
        meta.setDisplayName(TextUtil.color(prefix + "&f" + entry.name()));
        List<String> lore = new ArrayList<>();
        if (!entry.valueText().isBlank()) lore.add(TextUtil.color("&7Valor: " + entry.valueText()));
        for (String line : entry.lore()) lore.add(TextUtil.color(line));
        if (position <= 3) lore.add(TextUtil.color("&8Destaque do pódio"));
        meta.setLore(lore);
    }

    private ItemStack infoItem(RankingType type, RankingSnapshot snapshot, int available, int page, int pages) {
        List<String> lore = new ArrayList<>();
        lore.add("&7Ranking: &f" + type.displayName());
        lore.add("&7Posições carregadas: &f" + available + "&7/&f" + config.guiLimit(type));
        lore.add("&7Página: &f" + (page + 1) + "&7/&f" + pages);
        lore.add("&7Atualização do cache: &f" + config.updateIntervalSeconds(type) + "s");
        lore.add("&8A GUI usa o cache; não consulta o banco a cada clique.");
        if (!snapshot.available()) lore.add("&cFonte de dados indisponível.");
        return simple(Material.BOOK, "&eInformações do ranking", lore);
    }

    private void decorate(Inventory inventory) {
        ItemStack glass = simple(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 0; slot < 9; slot++) if (slot != 4) inventory.setItem(slot, glass);
        for (int slot = 45; slot < 54; slot++) inventory.setItem(slot, glass);
    }

    private ItemStack simple(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(TextUtil.color(name));
        if (lore != null && !lore.isEmpty()) meta.setLore(lore.stream().map(TextUtil::color).toList());
        item.setItemMeta(meta);
        return item;
    }
}
