package com.minezone.display.event;

public record EventStatusSnapshot(
        boolean systemAvailable,
        boolean active,
        String eventName,
        String state,
        int countdownSeconds,
        int participants,
        int alive,
        int spectators,
        boolean pvpEnabled
) {
    public EventStatusSnapshot {
        eventName = eventName == null ? "" : eventName;
        state = state == null ? "" : state;
        countdownSeconds = Math.max(0, countdownSeconds);
        participants = Math.max(0, participants);
        alive = Math.max(0, alive);
        spectators = Math.max(0, spectators);
    }

    public static EventStatusSnapshot unavailable() {
        return new EventStatusSnapshot(false, false, "", "", 0, 0, 0, 0, false);
    }

    public static EventStatusSnapshot idle() {
        return new EventStatusSnapshot(true, false, "", "", 0, 0, 0, 0, false);
    }
}
