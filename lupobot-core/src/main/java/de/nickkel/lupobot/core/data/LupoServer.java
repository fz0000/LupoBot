package de.nickkel.lupobot.core.data;

import com.iwebpp.crypto.TweetNaclFast;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;

public class LupoServer {

    @Getter
    private final Guild guild;
    @Getter
    private final List<LupoPlugin> plugins = new ArrayList<>();
    @Getter
    private final String prefix, language;

    public LupoServer(Guild guild) {
        LupoBot.getInstance().getLogger().info("Loading server " + guild.getName() + " " + guild.getIdLong() + " ...");
        this.guild = guild;
        this.prefix = "?";
        this.language = "en_US";
        plugins.addAll(LupoBot.getInstance().getPlugins());
        LupoBot.getInstance().getServers().put(this.guild, this);
    }

    public String translate(LupoPlugin plugin, String key, Object... params) {
        String translation;
        if (plugin == null) {
            translation = LupoBot.getInstance().getLanguageHandler().translate(this.language, key, params); // get core language handler
        } else {
            translation = plugin.getLanguageHandler().translate(this.language, key, params); // get plugins language handler
        }
        translation = translation.replace("%prefix%", this.prefix).replace("\n", "\n");
        return  translation;
    }

    public static LupoServer getByGuild(Guild guild) {
        if(LupoBot.getInstance().getServers().containsKey(guild)) {
            return LupoBot.getInstance().getServers().get(guild);
        } else {
            return new LupoServer(guild);
        }
    }

    public String translatePluginName(LupoPlugin plugin) {
        if(plugin == null) {
            return "Core";
        }
        return this.translate(plugin, plugin.getInfo().name() + "_display-name");
    }

    public Member getMember(String arg) {
        try {
            if(arg.contains("#") && !arg.startsWith("@")) { // only works if name does not contain any spaces
                return this.guild.getMemberByTag(arg);
            } else if(arg.startsWith("@")) {
                String id = arg.replace("@", "").replace("<", "").replace(">", "").replace("!", "");
                return this.guild.getMemberByTag(id);
            } else {
                return this.guild.getMemberById(arg);
            }
        } catch(Exception e) {
            return null;
        }
    }
}
