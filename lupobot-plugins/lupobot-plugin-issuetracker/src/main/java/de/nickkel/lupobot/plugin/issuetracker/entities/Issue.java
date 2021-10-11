package de.nickkel.lupobot.plugin.issuetracker.entities;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.issuetracker.enums.IssuePriority;
import de.nickkel.lupobot.plugin.issuetracker.enums.IssueState;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.Button;
import org.kohsuke.github.GHIssueBuilder;
import org.kohsuke.github.GHLabel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Issue {

    @Getter
    private final IssueServer server;
    @Getter
    private final String type;
    @Getter
    private final String component;
    @Getter
    private final int id;
    @Getter
    private long messageId;
    @Getter
    private long channelId;
    @Getter
    private final long creatorId;

    public Issue(IssueServer server, int id) {
        this.server = server;
        this.id = id;
        this.type = (String) getIssueData("type");
        this.component = (String) getIssueData("component");
        this.channelId = getIssueLong("channelId");
        this.messageId = getIssueLong("messageId");
        this.creatorId = getIssueLong("creatorId");
    }

    public Message getMessage() {
        TextChannel channel = this.server.getGuild().getTextChannelById(this.channelId);
        if (channel == null) return null;
        return channel.retrieveMessageById(this.messageId).complete();
    }

    public void note(Member member, String message) {
        addComment(member, "📝", message);
    }

    public void attach(Member member, String message) {
        addComment(member, "🖼", message);
    }

    public void approve(Member member, String message, IssuePriority priority, boolean master) {
        String icon = "✅";
        if (master) {
            icon = "❗✅";
        }
        addComment(member, icon, message + " (" + priority.name() + ")");

        BasicDBObject approvalObject = (BasicDBObject) getIssueData("approvals");
        approvalObject.append(member.getId(), priority.name());

        if (approvalObject.keySet().size() == this.server.getNeededVotes() || master) {
            moveMessage(this.server.getChannel(IssueState.APPROVED, this.type), LupoColor.GREEN);
            createOnGitHub();
        }
    }

    public void decline(Member member, String message, boolean master) {
        String icon = "❌";
        if (master) {
            icon = "❗❌";
        }
        addComment(member, icon, message);

        List<Long> declines = (List<Long>) getIssueData("declines");
        declines.add(member.getIdLong());
        appendIssueData("declines", declines);

        if (declines.size() == this.server.getNeededVotes() || master) {
            moveMessage(this.server.getChannel(IssueState.DECLINED, this.type), LupoColor.RED);
        }
    }

    public boolean hasApproved(Member member) {
        BasicDBObject approvalObject = (BasicDBObject) getIssueData("approvals");
        return approvalObject.containsKey(member.getId());
    }

    public boolean hasDeclined(Member member) {
        List<Long> declines = (List<Long>) getIssueData("declines");
        return declines.contains(member.getIdLong());
    }

    public IssuePriority getAvgPriority() {
        BasicDBObject approvalObject = (BasicDBObject) getIssueData("approvals");
        List<IssuePriority> priorities = new ArrayList<>();
        int low = 0, medium = 0, high = 0;

        for (Object key : approvalObject.values()) {
            String keyString = (String) key;
            priorities.add(IssuePriority.valueOf((keyString.toUpperCase())));
        }
        for (IssuePriority priority : priorities) {
            switch (priority) {
                case LOW:
                    low++;
                case MEDIUM:
                    medium++;
                case HIGH:
                    high++;
            }
        }

        int max = Collections.max(Arrays.asList(low, medium, high));
        if (max == high) {
            return IssuePriority.HIGH;
        } else if (max == medium) {
            return IssuePriority.MEDIUM;
        } else {
            return IssuePriority.LOW;
        }
    }

    public void moveMessage(TextChannel channel, LupoColor color) {
        Message message = getMessage();
        EmbedBuilder builder = new EmbedBuilder(message.getEmbeds().get(0));
        builder.setColor(color.getColor());
        if (channel.getIdLong() != this.channelId) {
            channel.sendMessageEmbeds(builder.build()).queue(success -> {
                setChannelId(success.getChannel().getIdLong());
                setMessageId(success.getIdLong());
            });
        } else {
            message.editMessageEmbeds(builder.build()).queue();
        }
        delete();
    }

    public void addComment(Member member, String icon, String comment) {
        Message message = getMessage();
        MessageEmbed embed = message.getEmbeds().get(0);
        EmbedBuilder builder = new EmbedBuilder(embed);

        MessageEmbed.Field commentField = builder.getFields().get(builder.getFields().size()-1);
        builder.getFields().remove(builder.getFields().size()-1);
        builder.addField(commentField.getName(), commentField.getValue() + "\n" + icon + " **" + member.getUser().getName() + ":** " + comment, false);

        message.editMessageEmbeds(builder.build()).queue();
    }

    public void createOnGitHub() {
        try {
            Message message = getMessage();
            MessageEmbed embed = message.getEmbeds().get(0);

            String body = embed.getAuthor().getName() + "\n\n";
            for (MessageEmbed.Field field : embed.getFields()) {
                body += "**" + field.getName() + "**" + "\n" + field.getValue() + "\n\n";
            }

            List<String> labels = new ArrayList<>();
            for (GHLabel label : this.server.getGitHubRepository().listLabels().toList()) {
                labels.add(label.getName());
            }

            GHIssueBuilder builder = this.server.getGitHubRepository().createIssue(embed.getFields().get(0).getValue()).body(body);
            if (labels.contains(this.type.toLowerCase())) builder.label(this.type.toLowerCase());
            if (labels.contains("c: " + this.component.toLowerCase())) builder.label("c: " + this.component.toLowerCase());
            if (labels.contains("p: " + getAvgPriority().name().toLowerCase())) builder.label("p: " + getAvgPriority().name().toLowerCase());

            Message msg = getMessage();
            msg.editMessage(msg).setActionRow(Button.link(builder.create().getHtmlUrl().toString(), "GitHub issue")).queue();
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
    }

    private void setMessageId(long id) {
        this.messageId = id;
        this.appendIssueData("messageId", id);
    }

    private void setChannelId(long id) {
        this.channelId = id;
        this.appendIssueData("channelId", id);
    }

    public void delete() {
        ((BasicDBObject) this.server.getServer().getPluginData(this.getServer().getPlugin(), "issues")).remove(String.valueOf(this.id));
    }

    private void appendIssueData(String key, Object val) {
        BasicDBObject dbObject = ((BasicDBObject) ((BasicDBObject) this.server.getServer().getPluginData(this.getServer().getPlugin(), "issues"))
                .get(String.valueOf(this.id))).append(key, val);
        dbObject.append(key, val);
    }

    private Object getIssueData(String key) {
        BasicDBObject dbObject = ((BasicDBObject) ((BasicDBObject) this.server.getServer().getPluginData(this.getServer().getPlugin(), "issues"))
                .get(String.valueOf(this.id)));
        return dbObject.get(key);
    }

    private long getIssueLong(String key) {
        BasicDBObject dbObject = ((BasicDBObject) ((BasicDBObject) this.server.getServer().getPluginData(this.getServer().getPlugin(), "issues"))
                .get(String.valueOf(this.id)));
        return dbObject.getLong(key);
    }
}