package de.nickkel.lupobot.core.data;

import com.mongodb.*;
import com.mongodb.util.JSON;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.StaffGroup;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;

public class LupoUser {

    @Getter
    private final long id;
    @Getter
    private final String asMention;
    @Getter
    private final boolean isBot;
    @Getter
    private final Map<LupoCommand, Long> cooldowns = new HashMap<>();
    @Getter
    private BasicDBObject data;

    public LupoUser(long id) {
        this.id = id;
        User discordUser = LupoBot.getInstance().getShardManager().retrieveUserById(id).complete();
        this.asMention = discordUser.getAsMention();
        this.isBot = discordUser.isBot();
        LupoBot.getInstance().getLogger().info("Loading user " + discordUser.getAsTag() + " (" + id + ") ...");

        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("name").getAsString());
        DBCollection collection = database.getCollection("users");
        DBObject query = new BasicDBObject("_id", id);
        DBCursor cursor = collection.find(query);

        try {
            BasicDBObject dbObject = (BasicDBObject) JSON.parse(LupoBot.getInstance().getUserConfig().convertToJsonString());
            dbObject.append("_id", id);
            collection.insert(dbObject);
            this.data = dbObject;
        } catch (DuplicateKeyException e) {
            this.data = (BasicDBObject) cursor.one();
        }

        // merge missing core data
        for (String key : LupoBot.getInstance().getUserConfig().getJsonObject().keySet()) {
            if (!this.data.containsKey(key)) {
                this.data.append(key, JSON.parse(new Document(LupoBot.getInstance().getUserConfig().getJsonElement(key).getAsJsonObject()).convertToJsonString()));
            }
        }

        // merge missing plugin data
        for (LupoPlugin plugin : LupoBot.getInstance().getPlugins()) {
            if (plugin.getUserConfig() != null) {
                if (!this.data.containsKey(plugin.getInfo().name())) {
                    this.data.append(plugin.getInfo().name(), JSON.parse(new Document(plugin.getUserConfig().getJsonObject()).convertToJsonString()));
                } else {
                    BasicDBObject dbObject = (BasicDBObject) this.data.get(plugin.getInfo().name());
                    for (String key : plugin.getUserConfig().getJsonObject().keySet()) {
                        if (!dbObject.containsKey(key)) {
                            dbObject.append(key, JSON.parse(new Document(plugin.getUserConfig().getJsonElement(key).getAsJsonObject()).convertToJsonString()));
                        }
                    }
                }
            }
        }

        LupoBot.getInstance().getUsers().put(this.id, this);
    }

    public boolean isStaff() {
        return this.getStaffGroup().getPower() != 0;
    }

    public StaffGroup getStaffGroup() {
        Member member = LupoBot.getInstance().getHub().retrieveMemberById(this.id).complete();
        Role role = ((member.getRoles().size() > 0) ? member.getRoles().get(0) : null);
        Document groups = new Document(LupoBot.getInstance().getConfig().getJsonElement("staffGroups").getAsJsonObject());
        if (role != null && groups.has(role.getId())) {
            return new StaffGroup(role);
        } else {
            return new StaffGroup(null);
        }
    }

    public void appendPluginData(LupoPlugin plugin, String key, Object val) {
        BasicDBObject dbObject = (BasicDBObject) this.data.get(plugin.getInfo().name());
        dbObject.append(key, val);
    }

    public Object getPluginData(LupoPlugin plugin, String key) {
        BasicDBObject dbObject = (BasicDBObject) this.data.get(plugin.getInfo().name());
        return dbObject.get(key);
    }

    public Long getPluginLong(LupoPlugin plugin, String key) {
        BasicDBObject dbObject = (BasicDBObject) this.data.get(plugin.getInfo().name());
        return dbObject.getLong(key);
    }

    public void saveData() {
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("name").getAsString());
        DBCollection collection = database.getCollection("users");
        DBObject query = new BasicDBObject("_id", this.id);
        collection.update(query, this.data);
    }

    public static LupoUser getByDiscordUser(User discordUser) {
        LupoUser user;
        if (LupoBot.getInstance().getUsers().containsKey(discordUser.getIdLong())) {
            user = LupoBot.getInstance().getUsers().get(discordUser.getIdLong());
        } else {
            user = new LupoUser(discordUser.getIdLong());
        }
        saveQueue(user);
        return user;
    }

    public static LupoUser getByMember(Member member) {
        return getByDiscordUser(member.getUser());
    }

    public static LupoUser getById(long id) {
        User discordUser;
        try {
            discordUser = LupoBot.getInstance().getShardManager().retrieveUserById(id).complete();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return getByDiscordUser(discordUser);
    }

    public static void saveQueue(LupoUser user) {
        if (!LupoBot.getInstance().getSaveQueuedUsers().contains(user)) {
            LupoBot.getInstance().getSaveQueuedUsers().add(user);
        }
    }
}
