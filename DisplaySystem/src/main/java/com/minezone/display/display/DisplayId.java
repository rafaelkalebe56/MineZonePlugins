package com.minezone.display.display;

import com.minezone.display.ranking.RankingType;

import java.util.Locale;

public enum DisplayId {
    RTP("rtp", "rtp", null, true),
    CONTINUAR("continuar", "continuar", null, true),
    TOP_OVO("top_ovo", "tops.ovo", RankingType.OVO, false),
    TOP_DINHEIRO("top_dinheiro", "tops.dinheiro", RankingType.DINHEIRO, false),
    TOP_CLAS("top_clas", "tops.clas", RankingType.CLAS, false);

    private final String commandKey;
    private final String configPath;
    private final RankingType rankingType;
    private final boolean animated;

    DisplayId(String commandKey, String configPath, RankingType rankingType, boolean animated) {
        this.commandKey = commandKey;
        this.configPath = configPath;
        this.rankingType = rankingType;
        this.animated = animated;
    }

    public String commandKey() { return commandKey; }
    public String configPath() { return configPath; }
    public RankingType rankingType() { return rankingType; }
    public boolean isRanking() { return rankingType != null; }
    public boolean animated() { return animated; }

    public static DisplayId fromCommand(String value) {
        if (value == null) return null;
        String clean = value.trim().toLowerCase(Locale.ROOT);
        for (DisplayId id : values()) if (id.commandKey.equals(clean)) return id;
        return null;
    }
}
