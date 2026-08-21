package com.minezone.display.event;

import java.util.Locale;

public final class EventDisplayText {
    private EventDisplayText() {}

    public static String format(String title, String subtitle, EventStatusSnapshot status) {
        StringBuilder text = new StringBuilder(nullToEmpty(title));
        if (status == null || !status.systemAvailable()) {
            return text.append("\n\n&cSistema de eventos indisponível").toString();
        }
        if (!status.active()) {
            text.append("\n\n&7Nenhum evento ativo agora");
            appendSubtitle(text, subtitle);
            return text.toString();
        }

        text.append("\n&f").append(status.eventName());
        switch (status.state().toUpperCase(Locale.ROOT)) {
            case "CREATING" -> text.append("\n&ePreparando o mundo do evento...");
            case "WAITING" -> {
                text.append("\n&aInscrições abertas");
                appendCountdown(text, status.countdownSeconds());
                text.append("\n&7Participantes: &f").append(status.participants());
                text.append("\n&eClique para entrar");
            }
            case "EXPLAINING" -> {
                text.append("\n&eExplicando as regras");
                text.append("\n&7Participantes: &f").append(status.participants());
            }
            case "PREPARING" -> {
                text.append("\n&ePreparação");
                appendCountdown(text, status.countdownSeconds());
                text.append("\n&7Vivos: &f").append(status.alive());
            }
            case "RUNNING" -> {
                text.append("\n&aEvento em andamento");
                text.append("\n&7Vivos: &f").append(status.alive());
                if (status.spectators() > 0) {
                    text.append(" &8• &7Assistindo: &f").append(status.spectators());
                }
                text.append("\n&7PvP: ").append(status.pvpEnabled() ? "&aATIVADO" : "&cDESATIVADO");
                text.append("\n&eClique para acompanhar");
            }
            case "ENDING" -> text.append("\n&eEvento sendo encerrado...");
            default -> text.append("\n&eEstado: &f").append(status.state());
        }
        return text.toString();
    }

    private static void appendCountdown(StringBuilder text, int seconds) {
        if (seconds > 0) text.append(" &8• &f").append(seconds).append('s');
    }

    private static void appendSubtitle(StringBuilder text, String subtitle) {
        String clean = nullToEmpty(subtitle);
        if (!clean.isBlank()) text.append('\n').append(clean);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
