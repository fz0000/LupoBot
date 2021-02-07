package de.nickkel.lupobot.core.plugin;

import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.language.LanguageHandler;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.hooks.EventListener;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class LupoPlugin {

    @Getter
    private static LupoPlugin instance;
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
    private List<EventListener> listeners = new ArrayList<>();
    @Getter @Setter
    private List<LupoCommand> commands = new ArrayList<>();

    public LupoPlugin() {
        instance = this;
    }

    public abstract void onEnable();

    public abstract void onDisable();

}
