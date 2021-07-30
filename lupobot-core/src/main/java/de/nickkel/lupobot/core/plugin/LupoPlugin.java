package de.nickkel.lupobot.core.plugin;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.language.LanguageHandler;
import de.nickkel.lupobot.core.util.FileResourcesUtils;
import de.nickkel.lupobot.core.util.ListenerRegister;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data public abstract class LupoPlugin {

    @Getter @Setter
    private LanguageHandler languageHandler;
    @Getter @Setter
    private Class resourcesClass;
    @Getter
    private final PluginInfo info = this.getClass().getAnnotation(PluginInfo.class);
    @Getter @Setter
    private boolean enabled;
    @Getter @Setter
    private Path path;
    @Getter @Setter
    private List<ListenerAdapter> listeners = new ArrayList<>();
    @Getter @Setter
    private List<LupoCommand> commands = new ArrayList<>();
    @Getter
    private Document userConfig, serverConfig, botConfig;

    public abstract void onEnable();

    public abstract void onDisable();

    public void registerCommands(String packageName) {
        LupoBot.getInstance().getCommandHandler().registerCommands(this, packageName);
    }

    public void registerListeners(String packageName) {
        new ListenerRegister(this, packageName);
    }

    public void registenerListener(Object listener) {
        LupoBot.getInstance().getShardManager().addEventListener(listener);
        this.listeners.add((ListenerAdapter) listener);
    }

    public void loadResources() {
        try {
            this.userConfig = new Document(new FileResourcesUtils(this.resourcesClass).getFileFromResourceAsStream("user.json"));
            if (this.userConfig.convertToJsonString().equals(LupoBot.getInstance().getUserConfig().convertToJsonString())) {
                this.userConfig = null;
            }
        } catch (IllegalArgumentException e) {
            LupoBot.getInstance().getLogger().warn("Could not find user.json file of plugin " + this.info.name());
        }
        try {
            this.serverConfig = new Document(new FileResourcesUtils(this.resourcesClass).getFileFromResourceAsStream("server.json"));
            if (this.serverConfig.convertToJsonString().equals(LupoBot.getInstance().getServerConfig().convertToJsonString())) {
                this.serverConfig = null;
            }
        } catch (IllegalArgumentException e) {
            LupoBot.getInstance().getLogger().warn("Could not find server.json file of plugin  " + this.info.name());
        }
        try {
            this.botConfig = new Document(new FileResourcesUtils(this.resourcesClass).getFileFromResourceAsStream("bot.json"));
            if (this.botConfig.convertToJsonString().equals(new Document(new FileResourcesUtils(LupoBot.class).getFileFromResourceAsStream("bot.json")).convertToJsonString())) {
                this.botConfig = null;
            }
        } catch (IllegalArgumentException e) {
            LupoBot.getInstance().getLogger().warn("Could not find bot.json file of plugin  " + this.info.name());
        }
    }
}
