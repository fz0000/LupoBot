package de.nickkel.lupobot.core.data;

import com.mongodb.*;
import com.mongodb.util.JSON;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
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
        LupoBot.getInstance().getLogger().info("Loading user " + discordUser.getAsTag() + " " + id + " ...");

        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        DBCollection collection = database.getCollection("users");
        DBObject query = new BasicDBObject("_id", id);
        DBCursor cursor = collection.find(query);

        try {
            BasicDBObject dbObject = (BasicDBObject) JSON.parse(LupoBot.getInstance().getUserConfig().convertToJsonString());
            dbObject.append("_id", id);
            collection.insert(dbObject);
            this.data = dbObject;
        } catch(DuplicateKeyException e) {
            this.data = (BasicDBObject) cursor.one();
        }
        LupoBot.getInstance().getUsers().put(this.id, this);
    }

    public void saveData() {
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        DBCollection collection = database.getCollection("users");
        DBObject query = new BasicDBObject("_id", this.id);
        DBCursor cursor = collection.find(query);
        collection.update(cursor.one(), this.data);
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
