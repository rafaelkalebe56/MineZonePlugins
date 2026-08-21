package com.minezone.display;

import com.minezone.display.animation.AnimationManager;
import com.minezone.display.command.DisplayAdminCommand;
import com.minezone.display.config.DisplayConfig;
import com.minezone.display.display.DisplayRegistry;
import com.minezone.display.display.LobbyDisplayManager;
import com.minezone.display.gui.RankingGUIService;
import com.minezone.display.hook.AuthHook;
import com.minezone.display.hook.EventHook;
import com.minezone.display.hook.RankHook;
import com.minezone.display.interaction.ActionExecutor;
import com.minezone.display.interaction.InteractionManager;
import com.minezone.display.ranking.RankingManager;
import com.minezone.display.ranking.provider.ClanRankingProvider;
import com.minezone.display.ranking.provider.EconomyRankingProvider;
import com.minezone.display.ranking.provider.EggRankingProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class DisplaySystemPlugin extends JavaPlugin {
    private DisplayConfig displayConfig;
    private RankingManager rankingManager;
    private LobbyDisplayManager lobbyDisplayManager;
    private ActionExecutor actionExecutor;

    @Override
    public void onEnable() {
        displayConfig = new DisplayConfig(this);
        displayConfig.reload();

        AuthHook authHook = new AuthHook();
        RankHook rankHook = new RankHook();
        EventHook eventHook = new EventHook();

        rankingManager = new RankingManager(this, displayConfig, List.of(
                new EggRankingProvider(),
                new EconomyRankingProvider(),
                new ClanRankingProvider()
        ));
        RankingGUIService rankingGUI = new RankingGUIService(this, displayConfig, rankingManager);
        actionExecutor = new ActionExecutor(this, rankingGUI);
        DisplayRegistry registry = new DisplayRegistry(this);
        AnimationManager animationManager = new AnimationManager(this, displayConfig);
        lobbyDisplayManager = new LobbyDisplayManager(
                this, displayConfig, registry, rankingManager, animationManager, eventHook);
        InteractionManager interactions = new InteractionManager(displayConfig, registry, authHook, actionExecutor);

        Bukkit.getPluginManager().registerEvents(rankingGUI, this);
        Bukkit.getPluginManager().registerEvents(interactions, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        DisplayAdminCommand adminCommand = new DisplayAdminCommand(
                displayConfig, lobbyDisplayManager, rankingManager, authHook, rankHook);
        PluginCommand command = getCommand("displayadmin");
        if (command != null) {
            command.setExecutor(adminCommand);
            command.setTabCompleter(adminCommand);
        } else {
            getLogger().severe("Comando /displayadmin não foi encontrado no plugin.yml.");
        }

        rankingManager.setChangeListener(lobbyDisplayManager::updateRanking);
        lobbyDisplayManager.start();
        rankingManager.start();

        getLogger().info("DisplaySystem ativado: 6 displays configuráveis, cache de rankings e interações carregados.");
    }

    @Override
    public void onDisable() {
        if (rankingManager != null) rankingManager.stop();
        if (lobbyDisplayManager != null) lobbyDisplayManager.stop();
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this);
    }

    public DisplayConfig getDisplayConfig() { return displayConfig; }
    public RankingManager getRankingManager() { return rankingManager; }
    public LobbyDisplayManager getLobbyDisplayManager() { return lobbyDisplayManager; }
    public ActionExecutor getActionExecutor() { return actionExecutor; }
}
