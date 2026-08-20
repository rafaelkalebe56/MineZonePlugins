package com.minezone.display.display;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.TextDisplay;

public final class DisplayBundle {
    private final DisplayId id;
    private final Display visual;
    private final TextDisplay text;
    private final Interaction interaction;
    private String lastTextKey;

    public DisplayBundle(DisplayId id, Display visual, TextDisplay text, Interaction interaction) {
        this.id = id;
        this.visual = visual;
        this.text = text;
        this.interaction = interaction;
    }

    public DisplayId id() { return id; }
    public Display visual() { return visual; }
    public TextDisplay text() { return text; }
    public Interaction interaction() { return interaction; }

    public boolean isValid() {
        return (visual == null || visual.isValid()) && text != null && text.isValid()
                && interaction != null && interaction.isValid();
    }

    public void updateText(String key, Component component) {
        if (text == null || !text.isValid()) return;
        if (key != null && key.equals(lastTextKey)) return;
        text.text(component);
        lastTextKey = key;
    }

    public void remove() {
        if (visual != null && visual.isValid()) visual.remove();
        if (text != null && text.isValid()) text.remove();
        if (interaction != null && interaction.isValid()) interaction.remove();
    }
}
