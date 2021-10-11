package de.nickkel.lupobot.plugin.issuetracker;

import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;
import de.nickkel.lupobot.plugin.issuetracker.entities.IssueServer;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import java.util.HashMap;
import java.util.Map;

@PluginInfo(name = "issuetracker", hidden = true, guildWhitelist = {352896116812939264L, 803268941144915978L}, author = "Nickkel")
public class LupoIssueTrackerPlugin extends LupoPlugin {

    @Getter
    public static LupoIssueTrackerPlugin instance;
    private final Map<Long, IssueServer> issueServers = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands("de.nickkel.lupobot.plugin.issuetracker.commands");
    }

    @Override
    public void onDisable() {

    }

    public IssueServer getIssueServer(Guild guild) {
        if (!this.issueServers.containsKey(guild.getIdLong())) {
            this.issueServers.put(guild.getIdLong(), new IssueServer(guild));
        }
        return this.issueServers.get(guild.getIdLong());
    }
}
