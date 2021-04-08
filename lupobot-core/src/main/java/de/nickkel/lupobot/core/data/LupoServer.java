package de.nickkel.lupobot.core.data;

import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.util.JSON;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LupoServer {

    @Getter
    private final Guild guild;
    @Getter
    private final List<LupoPlugin> plugins = new ArrayList<>();
    @Getter
    private String prefix, language;
    @Getter
    private BasicDBObject data;

    public LupoServer(Guild guild) {
        LupoBot.getInstance().getLogger().info("Loading server " + guild.getName() + " (" + guild.getIdLong() + ") " + "with " + guild.getMembers().size() + " members ...");
        this.guild = guild;

        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        DBCollection collection = database.getCollection("servers");
        DBObject query = new BasicDBObject("_id", guild.getIdLong());
        DBCursor cursor = collection.find(query);

        try {
            BasicDBObject dbObject = (BasicDBObject) JSON.parse(LupoBot.getInstance().getServerConfig().convertToJsonString());
            dbObject.append("_id", guild.getIdLong());
            // merge data file of all plugins into one file
            for (LupoPlugin plugin : LupoBot.getInstance().getPlugins()) {
                if (plugin.getServerConfig() != null) {
                    Document document = new Document(new JsonObject());
                    for (String key : plugin.getServerConfig().getJsonObject().keySet()) {
                        document.append(key, plugin.getServerConfig().getJsonElement(key));
                    }
                    BasicDBObject basic = (BasicDBObject) JSON.parse(document.convertToJsonString());
                    dbObject.append(plugin.getInfo().name(), basic);
                }
            }
            collection.insert(dbObject);
            this.data = dbObject;
        } catch (DuplicateKeyException e) {
            this.data = (BasicDBObject) cursor.one();
        }
        this.prefix = this.data.getString("prefix");
        this.language = this.data.getString("language");
        BasicDBList dbList = (BasicDBList) this.data.get("plugins");
        for (Object name : dbList) {
            if (LupoBot.getInstance().getPlugin((String) name) != null) {
                this.plugins.add(LupoBot.getInstance().getPlugin((String) name));
            } else { // remove plugin if it doesn't exist anymore
                dbList.remove(name);
                this.data.append("plugins", dbList);
            }
        }

        // merge missing plugin or core data if missing
        for (String key : LupoBot.getInstance().getServerConfig().getJsonObject().keySet()) {
            if (!this.data.containsKey(key)) {
                this.data.append(key, JSON.parse(new Document(LupoBot.getInstance().getServerConfig().getJsonElement(key).getAsJsonObject()).convertToJsonString()));
            }
        }
        for (LupoPlugin plugin : LupoBot.getInstance().getPlugins()) {
            if (plugin.getServerConfig() != null) {
                for (String key : plugin.getServerConfig().getJsonObject().keySet()) {
                    BasicDBObject dbObject = (BasicDBObject) this.data.get(plugin.getInfo().name());
                    if (!dbObject.containsKey(key)) {
                        BasicDBObject config = (BasicDBObject) JSON.parse(new Document(plugin.getServerConfig().getJsonObject()).convertToJsonString());
                        dbObject.append(key, config.get(key));
                        this.data.append(plugin.getInfo().name(), dbObject);
                    }
                }
            }
        }

        LupoBot.getInstance().getServers().put(this.guild, this);
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

    public void installPlugin(LupoPlugin plugin) {
        this.plugins.add(plugin);
        BasicDBList dbList = (BasicDBList) this.data.get("plugins");
        dbList.add(plugin.getInfo().name());
        this.data.append("plugins", dbList);
    }

    public void uninstallPlugin(LupoPlugin plugin) {
        this.plugins.remove(plugin);
        BasicDBList dbList = (BasicDBList) this.data.get("plugins");
        dbList.remove(plugin.getInfo().name());
        this.data.append("plugins", dbList);
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        this.data.append("prefix", prefix);
    }

    public void setLanguage(String language) {
        this.language = language;
        this.data.append("language", language);
    }

    public void saveData() {
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        DBCollection collection = database.getCollection("servers");
        DBObject query = new BasicDBObject("_id", guild.getIdLong());
        collection.update(query, this.data);
    }

    public String translate(LupoPlugin plugin, String key, Object... params) {
        String translation;
        if (plugin == null) {
            translation = LupoBot.getInstance().getLanguageHandler().translate(this.language, key, params); // get core language handler
        } else {
            translation = plugin.getLanguageHandler().translate(this.language, key, params); // get plugins language handler
        }
        translation = translation.replace("%prefix%", this.prefix).replace("\\n", "\n");
        return  translation;
    }

    public String translatePluginName(LupoPlugin plugin) {
        if(plugin == null) {
            return "Core";
        }
        return this.translate(plugin, plugin.getInfo().name() + "_display-name");
    }

    public Member getMember(String arg) {
        try {
            if (arg.contains("#") && !arg.startsWith("@")) { // only works if name does not contain any spaces
                return this.guild.getMemberByTag(arg);
            } else if (arg.startsWith("<@")) {
                String id = arg.replace("@", "").replace("<", "").replace(">", "").replace("!", "");
                if (id.length() == 18) {
                    return this.guild.getMemberById(id);
                } else {
                    return null;
                }
            } else {
                return this.guild.getMemberById(arg);
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public TextChannel getTextChannel(String arg) {
        if (arg.startsWith("<#")) {
            String id = arg.replace("#", "").replace("<", "").replace(">", "").replace("!", "");
            if (this.guild.getTextChannelById(id) != null) {
                return this.guild.getTextChannelById(id);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Role getRole(String arg) {
        if (arg.startsWith("<@&")) {
            String id = arg.replace("@", "").replace("<", "").replace(">", "").replace("&", "");
            if (this.guild.getRoleById(id) != null) {
                return this.guild.getRoleById(id);
            } else {
                return null;
            }
        } else if (this.guild.getRolesByName(arg, true).size() != 0) {
            return this.guild.getRolesByName(arg, true).get(0);
        } else {
            return null;
        }
    }

    public static LupoServer getByGuild(Guild guild) {
        LupoServer server = null;
        if (LupoBot.getInstance().getServers().containsKey(guild)) {
            server = LupoBot.getInstance().getServers().get(guild);
        } else {
            server = new LupoServer(guild);
        }
        saveQueue(server);
        return server;
    }

    public static LupoServer getById(long id) {
        LupoServer server;
        if (LupoBot.getInstance().getShardManager().getGuildById(id) == null) {
            return null;
        }
        if (LupoBot.getInstance().getServers().containsKey(LupoBot.getInstance().getShardManager().getGuildById(id))) {
            server =  LupoBot.getInstance().getServers().get(LupoBot.getInstance().getShardManager().getGuildById(id));
        } else {
            server = new LupoServer(LupoBot.getInstance().getShardManager().getGuildById(id));
        }
        saveQueue(server);
        return server;
    }

    public String formatLong(Long value) {
        Locale locale = new Locale(this.language.split("_")[0]);
        NumberFormat anotherFormat = NumberFormat.getNumberInstance(locale);

        if (anotherFormat instanceof DecimalFormat) {
            DecimalFormat anotherDFormat = (DecimalFormat) anotherFormat;
            anotherDFormat.setGroupingUsed(true);
            anotherDFormat.setGroupingSize(3);
            return anotherDFormat.format(value);
        }
        return String.valueOf(value);
    }

    public static void saveQueue(LupoServer server) {
        if (!LupoBot.getInstance().getSaveQueuedServers().contains(server)) {
            LupoBot.getInstance().getSaveQueuedServers().add(server);
        }
    }
}
