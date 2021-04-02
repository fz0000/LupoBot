package de.nickkel.lupobot.plugin.currency.data;

import lombok.Getter;

public class Item {

    @Getter
    private final String name, icon;
    @Getter
    private final long buy, sell;

    public Item(String name, String icon, Long buy, Long sell) {
        this.name = name;
        this.icon = icon;
        this.buy = buy;
        this.sell = sell;
    }
}
