package de.nickkel.lupobot.plugin.currency.entities;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

public class RichestList {

    @Getter
    private HashMap<Long, String> usersAsMention = new HashMap<>();
    @Getter
    private Long lastRefresh;
    private LinkedHashMap<Long, Long> sortedUsers;
    private FindIterable<Document> topDocuments;

    public RichestList() {
        loadFromDatabase();
    }

    public LinkedHashMap<Long, Long> getSortedUsers() {
        try {
            return this.sortedUsers;
        } finally {
            if (this.lastRefresh+1200000L-System.currentTimeMillis() < 0) {
                loadFromDatabase();
            }
        }
    }

    public void loadFromDatabase() {
        LupoBot.getInstance().getLogger().info("Loading currency richest list ...");
        MongoDatabase database = LupoBot.getInstance().getMongoClient().getDatabase(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        Bson sort = eq("currency.coins", -1L);

        MongoCollection<Document> collection = database.getCollection("users");
        this.topDocuments = collection.find()
                .sort(sort)
                .limit(20);

        Map<Long, Long> users = new HashMap<>();

        for (Document document : this.topDocuments) {
            BasicDBObject pluginDocument = new BasicDBObject((Document) document.get("currency"));
            users.put((long) document.get("_id"), pluginDocument.getLong("coins"));
        }

        this.sortedUsers = new LinkedHashMap<>();
        users.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> this.sortedUsers.put(x.getKey(), x.getValue()));
        for (Long id : this.sortedUsers.keySet()) {
            this.usersAsMention.put(id, LupoUser.getById(id).getAsMention());
        }
        this.lastRefresh = System.currentTimeMillis();
    }
}
