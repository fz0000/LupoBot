package de.nickkel.lupobot.plugin.currency.entities;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import lombok.Getter;

public class Job {

    @Getter
    private final String name, image;
    @Getter
    private final long duration, coins;
    @Getter
    private final Item neededItem;

    public Job(String name, Item neededItem, String image, long duration, long coins) {
        this.name = name;
        this.image = image;
        this.neededItem = neededItem;
        this.duration = duration;
        this.coins = coins;
    }

    public String getTranslatedName(LupoServer server) {
        return server.translate(LupoBot.getInstance().getPlugin(LupoCurrencyPlugin.getInstance().getInfo().name()), "currency_job-" + this.name + "-name");
    }

    public String getTranslatedDescription(LupoServer server) {
        return server.translate(LupoBot.getInstance().getPlugin(LupoCurrencyPlugin.getInstance().getInfo().name()), "currency_job-" + this.name + "-description");
    }
}
