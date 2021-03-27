package de.nickkel.lupobot.core.data;

import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.util.JSON;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;

public class LupoUser {

    @Getter
    private final long id;
    @Getter
    private final User discordUser;
    @Getter
    private final Map<LupoCommand, Long> cooldowns = new HashMap<>();
    @Getter
    private BasicDBObject data;

    public LupoUser(long id) {
        this.id = id;
        this.discordUser = LupoBot.getInstance().getShardManager().getUserById(id);
        LupoBot.getInstance().getLogger().info("Loading user " + discordUser.getAsTag() + " (" + id + ") ...");

        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        DBCollection collection = database.getCollection("users");
        DBObject query = new BasicDBObject("_id", id);
        DBCursor cursor = collection.find(query);

        try {
            BasicDBObject dbObject = (BasicDBObject) JSON.parse(LupoBot.getInstance().getUserConfig().convertToJsonString());
            dbObject.append("_id", id);
            // merge data file of all plugins into one file
            for(LupoPlugin plugin : LupoBot.getInstance().getPlugins()) {
                if(plugin.getUserConfig() != null) {
                    Document document = new Document(new JsonObject());
                    for(String key : plugin.getUserConfig().getJsonObject().keySet()) {
                        document.append(key, plugin.getUserConfig().getJsonElement(key));
                    }
                    BasicDBObject basic = (BasicDBObject) JSON.parse(document.convertToJsonString());
                    dbObject.append(plugin.getInfo().name(), basic);
                }
            }
            collection.insert(dbObject);
            this.data = dbObject;
        } catch(DuplicateKeyException e) {
            this.data = (BasicDBObject) cursor.one();
        }

        // TODO: set absent keys in the data from the default config file
        LupoBot.getInstance().getUsers().put(this.id, this);
    }

    public void appendPluginData(LupoPlugin plugin, String key, Object val) {
        BasicDBObject dbObject = (BasicDBObject) this.data.get(plugin.getInfo().name());
        dbObject.append(key, val);
    }

    public Object getPluginData(LupoPlugin plugin, String key) {
        BasicDBObject dbObject = (BasicDBObject) this.data.get(plugin.getInfo().name());
        return dbObject.get(key);
    }

    public void saveData() {
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        DBCollection collection = database.getCollection("users");
        DBObject query = new BasicDBObject("_id", this.id);
        collection.update(query, this.data);
    }

    public static LupoUser getByMember(Member member) {
        LupoUser user = null;
        if(LupoBot.getInstance().getUsers().containsKey(member.getIdLong())) {
            user =  LupoBot.getInstance().getUsers().get(member.getIdLong());
        } else {
            user = new LupoUser(member.getIdLong());
        }
        saveQueue(user);
        return user;
    }

    public static void saveQueue(LupoUser user) {
        if(!LupoBot.getInstance().getSaveQueuedUsers().contains(user)) {
            LupoBot.getInstance().getSaveQueuedUsers().add(user);
        }
    }
}
