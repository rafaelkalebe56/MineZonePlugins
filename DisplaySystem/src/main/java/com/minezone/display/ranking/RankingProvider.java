package com.minezone.display.ranking;

import java.util.List;

public interface RankingProvider {
    RankingType type();
    boolean isAvailable();
    List<RankingEntry> load(int limit) throws Exception;
    default List<String> loadSupplement() throws Exception { return List.of(); }
}
