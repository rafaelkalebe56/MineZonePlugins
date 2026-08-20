package com.minezone.display.interaction;

import com.minezone.display.DisplaySystemPlugin;
import com.minezone.display.gui.RankingGUIService;
import com.minezone.display.ranking.RankingType;
import com.minezone.display.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public final class ActionExecutor {
    private final DisplaySystemPlugin plugin;
    private final RankingGUIService rankingGUI;
    private final Map<DisplayActionType, DisplayActionHandler> handlers = new EnumMap<>(DisplayActionType.class);

    public ActionExecutor(DisplaySystemPlugin plugin, RankingGUIService rankingGUI) {
        this.plugin = plugin;
        this.rankingGUI = rankingGUI;
        registerBuiltIns();
    }

    public void registerHandler(DisplayActionType type, DisplayActionHandler handler) {
        if (type != null && handler != null) handlers.put(type, handler);
    }

    public void execute(Player player, DisplayAction action) {
        if (player == null || action == null || action.type() == DisplayActionType.NONE) return;
        DisplayActionHandler handler = handlers.get(action.type());
        if (handler == null) {
            player.sendMessage(TextUtil.color("&cEssa ação ainda não possui um executor registrado."));
            return;
        }
        handler.execute(player, action.value());
    }

    private void registerBuiltIns() {
        registerHandler(DisplayActionType.COMMAND, (player, value) -> {
            String command = normalizeCommand(value).replace("%player%", player.getName());
            if (command.isBlank()) return;
            Bukkit.dispatchCommand(player, command);
        });

        registerHandler(DisplayActionType.RANKING, (player, value) -> {
            RankingType type = RankingType.from(value);
            if (type == null) {
                player.sendMessage(TextUtil.color("&cRanking inválido no displays.yml."));
                return;
            }
            rankingGUI.open(player, type, 0);
        });

        registerHandler(DisplayActionType.GUI, (player, value) -> {
            String clean = value == null ? "" : value.trim();
            if (clean.toLowerCase(Locale.ROOT).startsWith("ranking:")) {
                RankingType type = RankingType.from(clean.substring("ranking:".length()));
                if (type != null) {
                    rankingGUI.open(player, type, 0);
                    return;
                }
            }
            player.sendMessage(TextUtil.color("&eGUI '" + clean + "' não está registrada neste DisplaySystem."));
        });

        registerHandler(DisplayActionType.TELEPORT, (player, value) -> {
            Location target = parseLocation(value);
            if (target == null) {
                player.sendMessage(TextUtil.color("&cDestino TELEPORT inválido. Use mundo,x,y,z,yaw,pitch."));
                return;
            }
            player.teleportAsync(target);
        });

        registerHandler(DisplayActionType.SERVER, (player, value) -> {
            String server = value == null ? "" : value.trim();
            if (server.isBlank()) return;
            try {
                byte[] payload = buildConnectPayload(server);
                player.sendPluginMessage(plugin, "BungeeCord", payload);
            } catch (Exception ex) {
                plugin.getLogger().warning("Falha ao enviar " + player.getName() + " para o servidor " + server + ": " + ex.getMessage());
                player.sendMessage(TextUtil.color("&cNão foi possível conectar ao servidor agora."));
            }
        });
    }

    private static String normalizeCommand(String value) {
        if (value == null) return "";
        String command = value.trim();
        while (command.startsWith("/")) command = command.substring(1);
        return command;
    }

    private static Location parseLocation(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String[] parts = raw.split(",");
        if (parts.length < 4) return null;
        try {
            World world = Bukkit.getWorld(parts[0].trim());
            if (world == null) return null;
            double x = Double.parseDouble(parts[1].trim());
            double y = Double.parseDouble(parts[2].trim());
            double z = Double.parseDouble(parts[3].trim());
            float yaw = parts.length >= 5 ? Float.parseFloat(parts[4].trim()) : 0F;
            float pitch = parts.length >= 6 ? Float.parseFloat(parts[5].trim()) : 0F;
            return new Location(world, x, y, z, yaw, pitch);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static byte[] buildConnectPayload(String server) throws Exception {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        try (java.io.DataOutputStream out = new java.io.DataOutputStream(bytes)) {
            out.writeUTF("Connect");
            out.writeUTF(server);
        }
        return bytes.toByteArray();
    }
}
