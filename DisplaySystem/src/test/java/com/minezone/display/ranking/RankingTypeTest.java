package com.minezone.display.ranking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RankingTypeTest {
    @Test
    void acceptsConfiguredAliases() {
        assertEquals(RankingType.OVO, RankingType.from("top_ovo"));
        assertEquals(RankingType.DINHEIRO, RankingType.from("SALDO"));
        assertEquals(RankingType.CLAS, RankingType.from("clans"));
    }

    @Test
    void rejectsUnknownRanking() {
        assertNull(RankingType.from("kills"));
    }
}
