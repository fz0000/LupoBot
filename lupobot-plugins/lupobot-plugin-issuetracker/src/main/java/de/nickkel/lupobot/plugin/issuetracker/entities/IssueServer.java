package de.nickkel.lupobot.plugin.issuetracker.entities;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.plugin.issuetracker.enums.IssueState;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import java.util.*;

public class IssueServer {

    @Getter
    private final Guild guild;
    @Getter
    private final LupoServer server;
    @Getter
    private final LupoPlugin plugin;
    @Getter
    private GitHub gitHub;
    @Getter
    private GHRepository gitHubRepository;
    @Getter
    private final Map<Long, IssueCreator> creators = new HashMap<>();

    public IssueServer(Guild guild) {
        this.plugin = LupoBot.getInstance().getPlugin("issuetracker");
        this.guild = guild;
        this.server = LupoServer.getByGuild(guild);

        try {
            this.gitHub = new GitHubBuilder().withOAuthToken(getOAuthToken()).build();
            this.gitHubRepository = this.gitHub.getRepository(getRepositoryName());
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

    }

    public Issue getIssue(int id) {
        if (((BasicDBObject) this.server.getPluginData(this.plugin, "issues")).containsKey(String.valueOf(id))) {
            return new Issue(this, id);
        } else {
            return null;
        }
    }

    public Set<String> getQuestionTypes() {
        return ((BasicDBObject) this.server.getPluginData(this.plugin, "types")).keySet();
    }

    public List<String> getComponents() {
        return (List<String>) this.server.getPluginData(this.plugin, "components");
    }

    public List<String> getQuestions(String questionType) {
        BasicDBObject dbObject = (BasicDBObject) this.server.getPluginData(this.plugin, "types");
        if (dbObject.containsKey(questionType)) {
            return (List<String>) ((BasicDBObject) dbObject.get(questionType)).get("questions");
        } else {
            return new ArrayList<>();
        }
    }

    public int getLastIssueId() {
        return (int) this.server.getPluginData(this.plugin, "lastIssueId");
    }

    public int getNeededVotes() {
        return (int) this.server.getPluginData(this.plugin, "neededVotes");
    }

    public TextChannel getCreationChannel() {
        long id = this.server.getPluginLong(this.getPlugin(), "creationChannel");
        if (id != -1) {
            TextChannel channel = this.server.getGuild().getTextChannelById(id);
            if (channel != null) {
                return channel;
            } else {
                this.server.appendPluginData(this.getPlugin(), "creationChannel", -1);
            }
        }
        return null;
    }

    public TextChannel getChannel(IssueState state, String type) {
        BasicDBObject dbObject = (BasicDBObject) this.server.getPluginData(this.plugin, "types");
        List<Long> channels = (List<Long>) ((BasicDBObject) dbObject.get(type)).get("channels");
        if (channels != null) {
            return this.guild.getTextChannelById(channels.get(state.ordinal()));
        }
        return null;
    }

    public String getOAuthToken() {
        return (String) this.server.getPluginData(this.plugin, "gitHubToken");
    }

    public String getRepositoryName() {
        return (String) this.server.getPluginData(this.plugin, "gitHubRepository");
    }
}
