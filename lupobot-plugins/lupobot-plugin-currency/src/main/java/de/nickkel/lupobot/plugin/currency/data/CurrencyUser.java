package de.nickkel.lupobot.plugin.currency.data;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import de.nickkel.lupobot.plugin.currency.entities.Item;
import de.nickkel.lupobot.plugin.currency.entities.Job;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.*;

import java.util.*;

public class CurrencyUser {

    @Getter
    private final LupoUser user;
    @Getter
    private final LupoPlugin plugin;
    @Getter @Setter
    private Job currentJob;

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
    }

    public Long getItem(Item item) {
        BasicDBObject pluginObject = (BasicDBObject) this.user.getData().get(LupoCurrencyPlugin.getInstance().getInfo().name());
        BasicDBObject itemObject = (BasicDBObject) pluginObject.get("inventory");
        if (itemObject.containsKey(item.getName())) {
            return itemObject.getLong(item.getName());
        } else {
            return 0L;
        }
    }

    public Item getRandomItem(int cheapPercent, int middlePercent, int expensivePercent) {
        List<Item> cheapItems = new ArrayList<>(), middleItems = new ArrayList<>(), expensiveItems = new ArrayList<>();
        for (Item item : LupoCurrencyPlugin.getInstance().getItems()) {
            if (item.getBuy() >= 2000) { // from 2000
                expensiveItems.add(item);
            } else if (item.getBuy() >= 500 && item.getBuy() < 2000) { // between 500-1999
                middleItems.add(item);
            } else if (item.getBuy() > 0 && item.getBuy() < 500) { // between 0-500
                cheapItems.add(item);
            }
        }

        int percent = new Random().nextInt(100);

        if (percent < cheapPercent){
            int index = new Random().nextInt(cheapItems.size());
            return cheapItems.get(index);
        } else if (percent < cheapPercent+middlePercent){
            int index = new Random().nextInt(middleItems.size());
            return middleItems.get(index);
        } else if (percent < cheapPercent+middlePercent+expensivePercent){
            int index = new Random().nextInt(expensiveItems.size());
            return expensiveItems.get(index);
        }
        return cheapItems.get(new Random().nextInt(cheapItems.size()));
    }

    public long getInventorySlots() {
        return this.user.getPluginLong(LupoBot.getInstance().getPlugin(LupoCurrencyPlugin.getInstance().getInfo().name()), "inventorySlots");
    }

    public void addInventorySlots(long amount) {
        this.user.appendPluginData(LupoBot.getInstance().getPlugin(LupoCurrencyPlugin.getInstance().getInfo().name()), "inventorySlots", getInventorySlots()+amount);
    }

    public long getUsedInventorySlots() {
        long usedSlots = 0;
        for (Item item : LupoCurrencyPlugin.getInstance().getItems()) {
            if (this.getItem(item) != 0) {
                usedSlots = usedSlots + this.getItem(item);
            }
        }
        return usedSlots;
    }

    public void addStreak() {
        this.user.appendPluginData(this.plugin, "dailyCoinStreak", this.user.getPluginLong(this.plugin, "dailyCoinStreak")+1);
    }

    public void setStreak(long streak) {
        this.user.appendPluginData(this.plugin, "dailyCoinStreak", streak);
    }
}
