package com.minezone.display.command;

import com.minezone.display.config.DisplayConfig;
import com.minezone.display.display.DisplayId;
import com.minezone.display.display.LobbyDisplayManager;
import com.minezone.display.hook.AuthHook;
import com.minezone.display.hook.RankHook;
import com.minezone.display.ranking.RankingManager;
import com.minezone.display.util.TextUtil;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class DisplayAdminCommand implements CommandExecutor, TabCompleter {
    private final DisplayConfig config;
    private final LobbyDisplayManager displays;
    private final RankingManager rankings;
    private final AuthHook authHook;
    private final RankHook rankHook;

    public DisplayAdminCommand(DisplayConfig config,
                               LobbyDisplayManager displays,
                               RankingManager rankings,
                               AuthHook authHook,
                               RankHook rankHook) {
        this.config = config;
        this.displays = displays;
        this.rankings = rankings;
        this.authHook = authHook;
        this.rankHook = rankHook;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Este comando administrativo deve ser usado por um jogador autenticado.");
            return true;
        }
        if (!checkAccess(player)) return true;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("reload")) {
            config.reload();
            displays.reloadAll();
            rankings.forceRefresh();
            player.sendMessage(TextUtil.color("&a✔ displays.yml recarregado e displays reconstruídos."));
            return true;
        }

        if (!sub.equals("set") && !sub.equals("reset")) {
            sendHelp(player);
            return true;
        }
        if (args.length < 2) {
            player.sendMessage(TextUtil.color("&cUse /displayadmin " + sub + " <rtp|continuar|top_ovo|top_dinheiro|top_clas>."));
            return true;
        }

        DisplayId id = DisplayId.fromCommand(args[1]);
        if (id == null) {
            player.sendMessage(TextUtil.color("&cDisplay inválido: &f" + args[1]));
            return true;
        }

        if (sub.equals("set")) {
            Location anchor = player.getLocation().clone().subtract(0D, 0.01D, 0D).getBlock().getLocation();
            config.setAnchor(id, anchor);
            displays.rebuild(id);
            player.sendMessage(TextUtil.color("&a✔ Âncora de &f" + id.commandKey() + " &asalva em &f"
                    + anchor.getWorld().getName() + " " + anchor.getBlockX() + " " + anchor.getBlockY() + " " + anchor.getBlockZ() + "&a."));
            return true;
        }

        config.resetAnchor(id);
        displays.rebuild(id);
        player.sendMessage(TextUtil.color("&a✔ &f" + id.commandKey() + " &avoltou para a coordenada padrão do plugin."));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!(sender instanceof Player player) || !canAccessSilently(player)) return List.of();
        if (args.length == 1) return filter(List.of("set", "reset", "reload"), args[0]);
        if (args.length == 2 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("reset"))) {
            return filter(Arrays.stream(DisplayId.values()).map(DisplayId::commandKey).toList(), args[1]);
        }
        return List.of();
    }

    private boolean checkAccess(Player player) {
        if (!authHook.isAvailable()) {
            player.sendMessage(TextUtil.color("&cAuthSystem indisponível. Comando bloqueado por segurança."));
            return false;
        }
        if (!authHook.isLogged(player)) {
            player.sendMessage(TextUtil.color("&cVocê precisa estar logado para administrar displays."));
            return false;
        }
        if (!rankHook.isAvailable()) {
            player.sendMessage(TextUtil.color("&cRankSystem indisponível. Comando bloqueado por segurança."));
            return false;
        }
        if (!rankHook.isDisplayAdmin(player)) {
            player.sendMessage(TextUtil.color("&cApenas DONO ou MOD podem administrar os displays."));
            return false;
        }
        return true;
    }

    private boolean canAccessSilently(Player player) {
        return authHook.isAvailable() && authHook.isLogged(player)
                && rankHook.isAvailable() && rankHook.isDisplayAdmin(player);
    }

    private void sendHelp(Player player) {
        player.sendMessage(TextUtil.color("&8&m--------------------------------"));
        player.sendMessage(TextUtil.color("&b&lDisplaySystem &7- Administração"));
        player.sendMessage(TextUtil.color("&f/displayadmin set <display> &7- salva o bloco sob seus pés."));
        player.sendMessage(TextUtil.color("&f/displayadmin reset <display> &7- restaura a posição padrão."));
        player.sendMessage(TextUtil.color("&f/displayadmin reload &7- recarrega displays.yml."));
        player.sendMessage(TextUtil.color("&8&m--------------------------------"));
    }

    private static List<String> filter(List<String> values, String prefix) {
        String clean = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(clean)).toList();
    }
}
