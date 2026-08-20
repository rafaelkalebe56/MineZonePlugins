package com.minezone.display.ranking;

import java.util.List;

public record RankingSnapshot(
        boolean available,
        List<RankingEntry> entries,
        List<String> supplement,
        String statusMessage
) {
    public RankingSnapshot {
        entries = entries == null ? List.of() : List.copyOf(entries);
        supplement = supplement == null ? List.of() : List.copyOf(supplement);
        if (statusMessage == null) statusMessage = "";
    }

    public static RankingSnapshot unavailable(String message) {
        return new RankingSnapshot(false, List.of(), List.of(), message);
    }

    public RankingSnapshot withSupplement(List<String> lines) {
        return new RankingSnapshot(available, entries, lines, statusMessage);
    }
}
