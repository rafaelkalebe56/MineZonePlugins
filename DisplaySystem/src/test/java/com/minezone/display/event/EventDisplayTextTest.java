package com.minezone.display.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EventDisplayTextTest {
    @Test
    void showsIdleStateAndConfiguredSubtitle() {
        String text = EventDisplayText.format("&6EVENTOS", "&7Clique para abrir", EventStatusSnapshot.idle());

        assertTrue(text.contains("Nenhum evento ativo"));
        assertTrue(text.contains("Clique para abrir"));
    }

    @Test
    void showsSignupCountdownAndParticipantCount() {
        EventStatusSnapshot status = new EventStatusSnapshot(
                true, true, "Spleef", "WAITING", 42, 7, 7, 0, false);

        String text = EventDisplayText.format("&6EVENTOS", "", status);

        assertTrue(text.contains("Spleef"));
        assertTrue(text.contains("Inscrições abertas"));
        assertTrue(text.contains("42s"));
        assertTrue(text.contains("Participantes: &f7"));
    }

    @Test
    void showsRunningDetails() {
        EventStatusSnapshot status = new EventStatusSnapshot(
                true, true, "Rei da Colina", "RUNNING", 0, 12, 5, 3, true);

        String text = EventDisplayText.format("&6EVENTOS", "", status);

        assertTrue(text.contains("Evento em andamento"));
        assertTrue(text.contains("Vivos: &f5"));
        assertTrue(text.contains("Assistindo: &f3"));
        assertTrue(text.contains("PvP: &aATIVADO"));
    }
}
