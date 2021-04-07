package de.nickkel.lupobot.plugin.profile.enums;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.plugin.profile.LupoProfilePlugin;
import lombok.Getter;

public enum Badge {
    TEST("😂");

    @Getter
    public final String emoji;

    private Badge(String emoji) {
        this.emoji = emoji;
    }

    public String getTranslatedName(LupoServer server) {
        return server.translate(LupoBot.getInstance().getPlugin(LupoProfilePlugin.getInstance().getInfo().name()), "profile_badge-" + this.name().toLowerCase());
    }
}
