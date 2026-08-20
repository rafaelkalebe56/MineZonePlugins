package com.minezone.display.ranking;

import java.util.Locale;

public enum RankingType {
    OVO("ovo", "Top Ovo"),
    DINHEIRO("dinheiro", "Top Dinheiro"),
    CLAS("clas", "Top Clãs");

    private final String key;
    private final String displayName;

    RankingType(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String key() { return key; }
    public String displayName() { return displayName; }

    public static RankingType from(String value) {
        if (value == null) return null;
        return switch (value.trim().toLowerCase(Locale.ROOT)) {
            case "ovo", "egg", "top_ovo", "topovo" -> OVO;
            case "dinheiro", "money", "saldo", "top_dinheiro", "topdinheiro" -> DINHEIRO;
            case "cla", "clas", "clan", "clans", "top_clas", "topclas" -> CLAS;
            default -> null;
        };
    }
}
