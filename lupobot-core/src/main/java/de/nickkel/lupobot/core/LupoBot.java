package de.nickkel.lupobot.core;

import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.command.CommandHandler;
import de.nickkel.lupobot.core.command.CommandListener;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Config;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.language.LanguageHandler;
import de.nickkel.lupobot.core.mysql.MySQL;
import de.nickkel.lupobot.core.pagination.method.Pages;
import de.nickkel.lupobot.core.pagination.model.PaginatorBuilder;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginLoader;
import de.nickkel.lupobot.core.util.FileResourcesUtils;
import lombok.Getter;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private MySQL mySQL;
    @Getter
    private LanguageHandler languageHandler;
    @Getter
    private CommandHandler commandHandler;
    @Getter
    private PluginLoader pluginLoader;
    @Getter
    private ExecutorService executorService;
    @Getter
    private Config config;
    @Getter
    private final List<LupoPlugin> plugins = new ArrayList<>();
    @Getter
    private final List<LupoCommand> commands = new ArrayList<>();
    @Getter
    private final Map<Guild, LupoServer> servers = new HashMap<>();
    @Getter
    private final Map<Long, LupoUser> users = new HashMap<>();
    @Getter
    private final List<String> availableLanguages = new ArrayList<>();
    @Getter
    private final long startMilis = System.currentTimeMillis();

    public static void main(String[] args) {
        new LupoBot().run(args);
    }

    public void run(String[] args) {
        instance = this;
        this.executorService = Executors.newCachedThreadPool();
        this.config = new Config(new FileResourcesUtils(this.getClass()).getFileFromResourceAsStream("config.json"));

        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(this.config.getString("token"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_PRESENCES)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(new CommandListener());

        this.languageHandler = new LanguageHandler(this.getClass());
        this.commandHandler = new CommandHandler();

        JsonObject jsonObject = this.config.getJsonElement("mysql").getAsJsonObject();
        this.mySQL = new MySQL(jsonObject.get("host").getAsString(), jsonObject.get("user").getAsString(), jsonObject.get("password").getAsString(),
                jsonObject.get("database").getAsString(), jsonObject.get("port").getAsString());
        //MySQL.connect();

        try {
            this.logger.info("Logging in ...");
            this.shardManager = builder.build();
            this.logger.info("Ready as " + this.shardManager.getShards().get(0).getSelfUser().getAsTag());
        } catch(LoginException e) {
            throw new RuntimeException("Failed to log in!", e);
        }

        Pages.activate(PaginatorBuilder.createSimplePaginator(this.shardManager));
        this.commandHandler.registerCommands(this.getClass().getClassLoader(), "de.nickkel.lupobot.core.internal.commands");
        this.pluginLoader = new PluginLoader();
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
