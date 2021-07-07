package de.nickkel.lupobot.core;

import com.github.ygimenez.exception.InvalidHandlerException;
import com.github.ygimenez.method.Pages;
import com.github.ygimenez.model.PaginatorBuilder;
import com.google.gson.JsonObject;
import com.mongodb.*;
import com.mongodb.util.JSON;
import de.nickkel.lupobot.core.command.CommandHandler;
import de.nickkel.lupobot.core.command.CommandListener;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.internal.listener.MaintenanceListener;
import de.nickkel.lupobot.core.language.LanguageHandler;
import de.nickkel.lupobot.core.pagination.PaginationListener;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginLoader;
import de.nickkel.lupobot.core.rest.RestServer;
import de.nickkel.lupobot.core.tasks.SaveDataTask;
import de.nickkel.lupobot.core.util.FileResourcesUtils;
import lombok.Getter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.util.*;

public class LupoBot {

    @Getter
    private static LupoBot instance;
    @Getter
    private final Logger logger = LoggerFactory.getLogger("LupoBot");
    @Getter
    private ShardManager shardManager;
    @Getter
    private LanguageHandler languageHandler;
    @Getter
    private CommandHandler commandHandler;
    @Getter
    private PluginLoader pluginLoader;
    @Getter
    private Document config, userConfig, serverConfig;
    @Getter
    private MongoClient mongoClient;
    @Getter
    private List<String> commandLineArgs;
    @Getter
    private BasicDBObject data;
    @Getter
    private final List<LupoPlugin> plugins = new ArrayList<>();
    @Getter
    private final List<LupoCommand> commands = new ArrayList<>();
    @Getter
    private final Map<Guild, LupoServer> servers = new HashMap<>();
    @Getter
    private final Map<Long, LupoUser> users = new HashMap<>();
    @Getter
    private final List<LupoServer> saveQueuedServers = new ArrayList<>();
    @Getter
    private final List<LupoUser> saveQueuedUsers = new ArrayList<>();
    @Getter
    private final List<String> availableLanguages = new ArrayList<>();
    @Getter
    private final long startMillis = System.currentTimeMillis();
    @Getter
    private RestServer restServer;
    @Getter
    private Timer dataServer;

    public static void main(String[] args) {
        new LupoBot().run(args);
    }

    public void run(String[] args) {
        instance = this;

        this.commandLineArgs = Arrays.asList(args);
        this.config = new Document(new File("configs/config.json")).loadDocument();
        System.out.println(Arrays.toString(Arrays.stream(new File("configs").listFiles()).toArray()));

        if (StartArguments.MAINTENANCE) {
            DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(this.config.getString("token"))
                    .addEventListeners(new MaintenanceListener())
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .setActivity(Activity.watching("Maintenance"));
            this.logger.info("Running in maintenance mode!");
            this.login(builder);
            return;
        }

        this.userConfig = new Document(new FileResourcesUtils(this.getClass()).getFileFromResourceAsStream("user.json"));
        this.serverConfig = new Document(new FileResourcesUtils(this.getClass()).getFileFromResourceAsStream("server.json"));

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(this.config.getString("token"))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setActivity(Activity.watching(this.config.getString("activity")));

        this.languageHandler = new LanguageHandler(this.getClass());
        this.commandHandler = new CommandHandler();

        this.mongoClient = new MongoClient(new MongoClientURI((LupoBot.getInstance().getConfig().getJsonElement("database").getAsJsonObject()).get("clientUri").getAsString()));
        this.login(builder);

        try {
            Pages.activate(PaginatorBuilder.createPaginator().setHandler(this.shardManager).shouldRemoveOnReact(true).build());
        } catch (InvalidHandlerException e) {
            e.printStackTrace();
        }
        this.commandHandler.registerCommands(this.getClass().getClassLoader(), "de.nickkel.lupobot.core.internal.commands");
        this.pluginLoader = new PluginLoader();
        this.loadBotData();

        this.dataServer = new Timer("DataSaver");
        this.dataServer.schedule(new SaveDataTask(), 600*1000, 600*1000);

        this.shardManager.addEventListener(new CommandListener(), new PaginationListener());
        this.logger.info("LupoBot is running on " + this.shardManager.getGuilds().size() + " servers");
        this.restServer = new RestServer();
    }

    private void login(DefaultShardManagerBuilder builder) {
        try {
            this.logger.info("Logging in ...");
            this.shardManager = builder.build();
            this.logger.info("Ready as " + this.shardManager.getShards().get(0).getSelfUser().getAsTag());
        } catch (LoginException e) {
            throw new RuntimeException("Failed to log in!", e);
        }
    }

    public Guild getHub() {
        return this.shardManager.getGuildById(this.config.getLong("supportServer"));
    }

    public LupoCommand getCommand(String name) {
        return this.commands.stream().filter((command -> command.getInfo().name().equalsIgnoreCase(name))).findFirst().orElse(null);
    }

    public LupoPlugin getPlugin(String name) {
        return this.plugins.stream().filter((plugin -> plugin.getInfo().name().equalsIgnoreCase(name))).findFirst().orElse(null);
    }

    public SelfUser getSelfUser() {
        return this.shardManager.getShards().get(0).getSelfUser();
    }

    public Object getPluginData(LupoPlugin plugin, String key) {
        BasicDBObject dbObject = (BasicDBObject) this.data.get(plugin.getInfo().name());
        return dbObject.get(key);
    }

    private void loadBotData() {
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("name").getAsString());
        DBCollection collection = database.getCollection("bot");
        DBObject query = new BasicDBObject("_id", this.getSelfUser().getIdLong());
        DBCursor cursor = collection.find(query);
        Document botConfig = new Document(new FileResourcesUtils(this.getClass()).getFileFromResourceAsStream("bot.json"));
        try {
            BasicDBObject dbObject = (BasicDBObject) JSON.parse(botConfig.convertToJsonString());
            dbObject.append("_id", this.getSelfUser().getIdLong());
            collection.insert(dbObject);
            this.data = dbObject;
        } catch(DuplicateKeyException e) {
            this.data = (BasicDBObject) cursor.one();
        }

        // merge missing core data
        for (String key : botConfig.getJsonObject().keySet()) {
            if (!this.data.containsKey(key)) {
                this.data.append(key, JSON.parse(new Document(botConfig.getJsonElement(key).getAsJsonObject()).convertToJsonString()));
            }
        }

        // merge missing plugin data
        for (LupoPlugin plugin : LupoBot.getInstance().getPlugins()) {
            if (plugin.getBotConfig() != null) {
                if (!this.data.containsKey(plugin.getInfo().name())) {
                    this.data.append(plugin.getInfo().name(), JSON.parse(new Document(plugin.getBotConfig().getJsonObject()).convertToJsonString()));
                } else {
                    BasicDBObject dbObject = (BasicDBObject) this.data.get(plugin.getInfo().name());
                    for (String key : plugin.getBotConfig().getJsonObject().keySet()) {
                        if (!dbObject.containsKey(key)) {
                            dbObject.append(key, JSON.parse(new Document(plugin.getBotConfig().getJsonElement(key).getAsJsonObject()).convertToJsonString()));
                        }
                    }
                }
            }
        }
    }

    public void saveData() {
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("name").getAsString());
        DBCollection collection = database.getCollection("bot");
        DBObject query = new BasicDBObject("_id", this.getSelfUser().getIdLong());
        collection.update(query, this.data);
    }
}
