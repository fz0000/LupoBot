package de.nickkel.lupobot.core;

import com.google.gson.JsonObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import de.nickkel.lupobot.core.command.CommandHandler;
import de.nickkel.lupobot.core.command.CommandListener;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.internal.listener.MaintenanceListener;
import de.nickkel.lupobot.core.language.LanguageHandler;
import de.nickkel.lupobot.core.pagination.method.Pages;
import de.nickkel.lupobot.core.pagination.model.PaginatorBuilder;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginLoader;
import de.nickkel.lupobot.core.tasks.SaveDataTask;
import de.nickkel.lupobot.core.util.FileResourcesUtils;
import lombok.Getter;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService executorService;
    @Getter
    private Document config, userConfig, serverConfig;
    @Getter
    private MongoClient mongoClient;
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

    public static void main(String[] args) {
        new LupoBot().run(args);
    }

    public void run(String[] args) {
        instance = this;
        this.executorService = Executors.newCachedThreadPool();
        if(new File("storage/config.json").exists()) {
            this.config = new Document(new File("storage/config.json"));
        } else {
            this.config = new Document(new FileResourcesUtils(this.getClass()).getFileFromResourceAsStream("config.json"));
        }

        for(String arg : args) {
            if(arg.equalsIgnoreCase("--maintenance")) {
                DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(this.config.getString("token"))
                        .addEventListeners(new MaintenanceListener())
                        .setStatus(OnlineStatus.DO_NOT_DISTURB)
                        .setActivity(Activity.watching("Maintenance"));
                this.logger.info("Running in maintenance mode!");
                this.login(builder);
                return;
            }
        }

        this.userConfig = new Document(new FileResourcesUtils(this.getClass()).getFileFromResourceAsStream("user.json"));
        this.serverConfig = new Document(new FileResourcesUtils(this.getClass()).getFileFromResourceAsStream("server.json"));

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(this.config.getString("token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(new CommandListener())
                .setActivity(Activity.watching("?help"));

        this.languageHandler = new LanguageHandler(this.getClass());
        this.commandHandler = new CommandHandler();

        this.mongoClient = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
        this.login(builder);

        Pages.activate(PaginatorBuilder.createSimplePaginator(this.shardManager));
        this.commandHandler.registerCommands(this.getClass().getClassLoader(), "de.nickkel.lupobot.core.internal.commands");
        this.pluginLoader = new PluginLoader();

        Timer timer = new Timer("DataSaver");
        timer.schedule(new SaveDataTask(), 600*1000, 600*1000);
        this.logger.info("LupoBot is running on " + this.shardManager.getGuilds().size() + " servers");
    }

    private void login(DefaultShardManagerBuilder builder) {
        try {
            this.logger.info("Logging in ...");
            this.shardManager = builder.build();
            this.logger.info("Ready as " + this.shardManager.getShards().get(0).getSelfUser().getAsTag());
        } catch(LoginException e) {
            throw new RuntimeException("Failed to log in!", e);
        }
    }

    public LupoPlugin getPlugin(String name) {
        for(LupoPlugin plugin : this.plugins) {
            if(plugin.getInfo().name().equalsIgnoreCase(name)) {
                return plugin;
            }
        }
        return null;
    }

    public SelfUser getSelfUser() {
        return this.shardManager.getShards().get(0).getSelfUser();
    }
}
