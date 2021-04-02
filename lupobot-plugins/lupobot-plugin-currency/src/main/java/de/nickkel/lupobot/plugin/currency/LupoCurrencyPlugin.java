package de.nickkel.lupobot.plugin.currency;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;
import de.nickkel.lupobot.core.util.FileResourcesUtils;
import de.nickkel.lupobot.plugin.currency.data.CurrencyUser;
import de.nickkel.lupobot.plugin.currency.data.Item;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PluginInfo(name = "currency", version = "1.0.0", author = "Nickkel")
public class LupoCurrencyPlugin extends LupoPlugin {

    @Getter
    private static LupoCurrencyPlugin instance;
    @Getter
    private Document config;
    @Getter
    private List<Item> items = new ArrayList<>();
    private final Map<Long, CurrencyUser> currencyUser = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        LupoBot.getInstance().getCommandHandler().registerCommands(this, "de.nickkel.lupobot.plugin.currency.commands");
        if(new File("storage/items.json").exists()) {
            this.config = new Document(new File("storage/items.json"));
        } else {
            this.config = new Document(new FileResourcesUtils(this.getClass()).getFileFromResourceAsStream("items.json"));
        }
        this.loadItems();
    }

    @Override
    public void onDisable() {

    }

    public void loadItems() {
        BasicDBObject dbObject = (BasicDBObject) JSON.parse(this.config.convertToJsonString());
        for(String name : dbObject.keySet()) {
            BasicDBList dbList = new BasicDBList();
            dbList.addAll(this.config.getList(name));
            Item item = new Item(name, (String) dbList.get(0), Long.parseLong((String) dbList.get(1)), Long.parseLong((String) dbList.get(2)));
            this.items.add(item);
            LupoBot.getInstance().getLogger().info("Loaded item " + item.getName());
        }
    }

    public Item getItem(String name) {
        for(Item item : this.items) {
            if(item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    public CurrencyUser getCurrencyUser(Member member) {
        if(!this.currencyUser.containsKey(member.getIdLong())) {
            this.currencyUser.put(member.getIdLong(), new CurrencyUser(member));
        }
        return this.currencyUser.get(member.getIdLong());
    }
}
