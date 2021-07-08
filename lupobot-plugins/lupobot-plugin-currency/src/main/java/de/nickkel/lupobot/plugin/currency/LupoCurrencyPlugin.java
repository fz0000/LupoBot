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
import de.nickkel.lupobot.plugin.currency.entities.Item;
import de.nickkel.lupobot.plugin.currency.entities.Job;
import de.nickkel.lupobot.plugin.currency.entities.RichestList;
import de.nickkel.lupobot.plugin.currency.task.DailyRemindTask;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;

import java.io.File;
import java.util.*;

@PluginInfo(name = "currency", author = "Nickkel")
public class LupoCurrencyPlugin extends LupoPlugin {

    @Getter
    private static LupoCurrencyPlugin instance;
    @Getter
    private Document itemConfig, jobConfig;
    @Getter
    private final List<Item> items = new ArrayList<>();
    @Getter
    private final List<Job> jobs = new ArrayList<>();
    @Getter
    private RichestList richestList;
    private final Map<Long, CurrencyUser> currencyUser = new HashMap<>();
    private Timer dailyRemindTask;

    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands("de.nickkel.lupobot.plugin.currency.commands");

        if (new File("configs/items.json").exists()) {
            this.itemConfig = new Document(new File("configs/items.json")).loadDocument();
        } else {
            this.itemConfig = new Document(new FileResourcesUtils(this.getClass()).getFileFromResourceAsStream("configs/items.json"));
        }
        if (new File("configs/jobs.json").exists()) {
            this.jobConfig = new Document(new File("configs/jobs.json")).loadDocument();
        } else {
            this.jobConfig = new Document(new FileResourcesUtils(this.getClass()).getFileFromResourceAsStream("configs/jobs.json"));
        }

        this.loadItems();
        this.loadJobs();

        this.dailyRemindTask = new Timer("DailyReminder");
        this.dailyRemindTask.schedule(new DailyRemindTask(), 600*1000, 3600*1000);
        this.richestList = new RichestList();
    }

    @Override
    public void onDisable() {
        this.dailyRemindTask.cancel();
    }

    public void loadItems() {
        BasicDBObject dbObject = (BasicDBObject) JSON.parse(this.itemConfig.convertToJsonString());
        for (String name : dbObject.keySet()) {
            BasicDBList dbList = new BasicDBList();
            dbList.addAll(this.itemConfig.getList(name));
            Item item = new Item(name, (String) dbList.get(0), Long.parseLong((String) dbList.get(1)), Long.parseLong((String) dbList.get(2)));
            this.items.add(item);
            LupoBot.getInstance().getLogger().info("Loaded item " + item.getName());
        }
    }

    public void loadJobs() {
        BasicDBObject dbObject = (BasicDBObject) JSON.parse(this.jobConfig.convertToJsonString());
        for (String name : dbObject.keySet()) {
            BasicDBList dbList = new BasicDBList();
            dbList.addAll(this.jobConfig.getList(name));
            Job job = new Job(name, this.getItem((String) dbList.get(0)), (String) dbList.get(1), Long.parseLong((String) dbList.get(2)), Long.parseLong((String) dbList.get(3)));
            this.jobs.add(job);
            LupoBot.getInstance().getLogger().info("Loaded job " + job.getName());
        }
    }

    public Item getItem(String name) {
        return this.items.stream().filter((item -> item.getName().equalsIgnoreCase(name))).findFirst().orElse(null);
    }

    public CurrencyUser getCurrencyUser(Member member) {
        if (!this.currencyUser.containsKey(member.getIdLong())) {
            this.currencyUser.put(member.getIdLong(), new CurrencyUser(member));
        }
        return this.currencyUser.get(member.getIdLong());
    }
}
