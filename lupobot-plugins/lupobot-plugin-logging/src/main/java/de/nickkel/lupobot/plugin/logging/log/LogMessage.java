package de.nickkel.lupobot.plugin.logging.log;

import com.mongodb.*;
import de.nickkel.lupobot.core.LupoBot;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;

public class LogMessage {

    @Getter
    public BasicDBObject data;
    @Getter
    public long id;

    public LogMessage(long messageId, boolean createIfNotExists) {
        this.id = messageId;
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        DBCollection collection = database.getCollection("messages");
        DBObject query = new BasicDBObject("_id", messageId);
        DBCursor cursor = collection.find(query);
        try {
            if (createIfNotExists) {
                BasicDBObject dbObject = new BasicDBObject();
                dbObject.append("_id", messageId);
                collection.insert(dbObject);
                this.data = dbObject;
            } else {
                this.data = (BasicDBObject) cursor.one();
            }
        } catch (DuplicateKeyException e) {
            this.data = (BasicDBObject) cursor.one();
        }
    }

    public void update(Message message) {
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        DBCollection collection = database.getCollection("messages");
        DBObject query = new BasicDBObject("_id", message.getIdLong());
        this.data.append("content", message.getContentRaw());
        this.data.append("authorAvatarUrl", message.getAuthor().getAvatarUrl());
        this.data.append("authorId", message.getAuthor().getIdLong());
        this.data.append("authorAsTag", message.getAuthor().getAsTag());
        collection.update(query, this.data);
    }

    public void delete() {
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        DBCollection collection = database.getCollection("messages");
        DBObject query = new BasicDBObject("_id", this.id);
        collection.remove(query);
    }

    public String get(String key) {
        return this.data.getString(key);
    }

    public boolean exists() {
        if(this.data.size() == 1) {
            delete();
            System.out.println("false");
            return false;
        } else {
            System.out.println("true");
            return true;
        }
    }
}
