package de.nickkel.lupobot.plugin.currency.data;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.pagination.model.Page;
import de.nickkel.lupobot.core.pagination.type.PageType;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurrencyUser {

    @Getter
    private final LupoUser user;
    @Getter
    private final LupoPlugin plugin;

    public CurrencyUser(Member member) {
        this.plugin = LupoBot.getInstance().getPlugin("currency");
        this.user = LupoUser.getByMember(member);
    }

    public long getCoins() {
        return this.user.getPluginLong(this.plugin, "coins");
    }

    public void addCoins(long amount) {
        this.user.appendPluginData(this.plugin, "coins", this.user.getPluginLong(this.plugin, "coins")+amount);
    }

    public void addItem(Item item, long amount) {
        BasicDBObject pluginObject = (BasicDBObject) this.user.getData().get(LupoCurrencyPlugin.getInstance().getInfo().name());
        BasicDBObject itemObject = (BasicDBObject) pluginObject.get("inventory");

        itemObject.append(item.getName(), getItem(item)+amount);
        pluginObject.append("inventory", itemObject);
        this.user.getData().append(LupoCurrencyPlugin.getInstance().getInfo().name(), pluginObject);
    }

    public Long getItem(Item item) {
        BasicDBObject pluginObject = (BasicDBObject) this.user.getData().get(LupoCurrencyPlugin.getInstance().getInfo().name());
        BasicDBObject itemObject = (BasicDBObject) pluginObject.get("inventory");
        if(itemObject.containsKey(item.getName())) {
            return itemObject.getLong(item.getName());
        } else {
            return 0L;
        }
    }

    public long getInventorySlots() {
        return this.user.getPluginLong(LupoBot.getInstance().getPlugin(LupoCurrencyPlugin.getInstance().getInfo().name()), "inventorySlots");
    }

    public void addInventorySlots(long amount) {
        this.user.appendPluginData(LupoBot.getInstance().getPlugin(LupoCurrencyPlugin.getInstance().getInfo().name()), "inventorySlots", getInventorySlots()+amount);
    }

    public long getUsedInventorySlots() {
        long usedSlots = 0;
        for(Item item : LupoCurrencyPlugin.getInstance().getItems()) {
            if(this.getItem(item) != 0) {
                usedSlots = usedSlots + this.getItem(item);
            }
        }
        return usedSlots;
    }

    public void addStreak() {
        this.user.appendPluginData(this.plugin, "dailyCoinStreak", this.user.getPluginLong(this.plugin, "dailyCoinStreak")+1);
    }
}
