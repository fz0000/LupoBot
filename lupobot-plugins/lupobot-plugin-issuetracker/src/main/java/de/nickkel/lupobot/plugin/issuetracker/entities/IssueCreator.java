package de.nickkel.lupobot.plugin.issuetracker.entities;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.issuetracker.LupoIssueTrackerPlugin;
import de.nickkel.lupobot.plugin.issuetracker.enums.IssueState;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import java.time.OffsetDateTime;
import java.util.*;

public class IssueCreator extends ListenerAdapter {

    @Getter
    private final IssueServer server;
    @Getter
    private final TextChannel channel;
    @Getter
    private final Member member;
    @Getter
    private String component, questionType;
    @Getter
    private Timer timer;
    @Getter
    private long lastMessage = -1;
    @Getter
    private int currentQuestion = 0;
    @Getter
    private final Map<String, String> answers = new LinkedHashMap<>();

    public IssueCreator(TextChannel channel, Member member) {
        this.channel = channel;
        this.member = member;

        LupoIssueTrackerPlugin.getInstance().registenerListener(this);
        this.server = LupoIssueTrackerPlugin.getInstance().getIssueServer(channel.getGuild());
        this.server.getCreators().put(member.getIdLong(), this);

        SelectionMenu.Builder menu = SelectionMenu.create("ISSUETRACKER;SELECT;COMPONENT;" + member.getId())
                .setPlaceholder(this.server.getServer().translate(this.server.getPlugin(), "issuetracker_choose-component-placeholder"))
                .setRequiredRange(1, 1);

        for (String component : this.server.getComponents()) {
            menu.addOption(component, component);
        }

        MessageBuilder builder = new MessageBuilder();
        builder.setContent(member.getAsMention());
        builder.setEmbeds(new EmbedBuilder()
                .setColor(LupoColor.BLUE.getColor())
                .setAuthor(member.getUser().getAsTag() + " (" + member.getId() + ")", null, member.getUser().getAvatarUrl())
                .setDescription(this.server.getServer().translate(this.server.getPlugin(), "issuetracker_choose-component"))
                .build());

        channel.sendMessage(builder.build()).setActionRow(menu.build()).queue();
        this.startTimeOutTimer();
    }

    public void nextQuestion() {
        this.timer.cancel();
        int max = this.server.getQuestions(this.questionType).size();

        if (this.lastMessage != -1) {
            this.channel.retrieveMessageById(this.lastMessage).queue(success -> {
                if (success != null) success.delete().queue();
            });
        }

        if (max != this.currentQuestion) {
            MessageBuilder builder = new MessageBuilder();
            builder.setContent(this.member.getAsMention());
            builder.setEmbeds(new EmbedBuilder()
                    .setColor(LupoColor.ORANGE.getColor())
                    .setAuthor(this.member.getUser().getAsTag() + " (" + this.member.getId() + ")", null, this. member.getUser().getAvatarUrl())
                    .setDescription(this.server.getQuestions(this.questionType).get(this.currentQuestion))
                    .setFooter(this.server.getServer().translate(this.server.getPlugin(), "issuetracker_question-footer", this.currentQuestion+1, max))
                    .setTimestamp(OffsetDateTime.now())
                    .build());
            this.channel.sendMessage(builder.build()).queue(success -> {
                this.lastMessage = success.getIdLong();
            });
            this.startTimeOutTimer();
        } else { // answered all questions
            TextChannel channel = this.server.getChannel(IssueState.OPENED, this.questionType);
            if (channel != null) {
                this.server.getServer().appendPluginData(this.server.getPlugin(), "lastIssueId", this.server.getLastIssueId()+1);
                int id = this.server.getLastIssueId();

                BasicDBObject issues = (BasicDBObject) this.server.getServer().getPluginData(this.server.getPlugin(), "issues");
                BasicDBObject issueObject = new BasicDBObject();
                issueObject.append("id", id);
                issueObject.append("state", IssueState.OPENED.name());
                issueObject.append("creatorId", this.member.getIdLong());
                issueObject.append("type", this.questionType);
                issueObject.append("component", this.component);
                issueObject.append("approvals", new BasicDBObject());
                issueObject.append("declines", new ArrayList<>());

                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(LupoColor.ORANGE.getColor());
                builder.setTimestamp(OffsetDateTime.now());
                builder.setAuthor(this.server.getServer().translate(this.server.getPlugin(), "issuetracker_issue-author", this.questionType, this.member.getUser().getAsTag(), this.member.getId()),
                        null, this.member.getUser().getAvatarUrl());
                builder.setDescription(this.server.getServer().translate(this.server.getPlugin(), "issuetracker_issue-description",
                        id, this.component));
                for (String question : this.answers.keySet()) {
                    builder.addField(question, this.answers.get(question), false);
                }
                channel.sendMessageEmbeds(builder.build()).queue(success -> {
                    issueObject.append("messageId", success.getIdLong());
                    issueObject.append("channelId", success.getTextChannel().getIdLong());
                });

                MessageBuilder mb = new MessageBuilder();
                mb.setContent(this.member.getAsMention());
                mb.setEmbeds(new EmbedBuilder()
                        .setColor(LupoColor.BLUE.getColor())
                        .setAuthor(this.member.getUser().getAsTag() + " (" + this.member.getId() + ")", null, this. member.getUser().getAvatarUrl())
                        .setDescription(this.server.getServer().translate(this.server.getPlugin(), "issuetracker_issue-created"))
                        .setTimestamp(OffsetDateTime.now())
                        .build());
                this.channel.sendMessage(mb.build()).queue();

                issues.append(String.valueOf(id), issueObject);
                this.server.getCreators().remove(this.member.getIdLong());
            }
        }
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getChannel().getIdLong() == this.channel.getIdLong() && event.getMember().getIdLong() == this.member.getIdLong() && this.questionType != null) {
            this.timer.cancel();
            this.answers.put(this.server.getQuestions(this.questionType).get(this.currentQuestion), event.getMessage().getContentRaw());
            this.currentQuestion++;
            event.getMessage().delete().queue();
            nextQuestion();
        }
    }

    @Override
    public void onSelectionMenu(SelectionMenuEvent event) {
        if (event.getInteraction().getComponentId().equals("ISSUETRACKER;SELECT;COMPONENT;" + event.getMember().getId())) { ;
            this.component = event.getValues().get(0);
            event.deferEdit().queue();
            event.getMessage().delete().queue();
            sendTypeSelection();
        } else if (event.getInteraction().getComponentId().equals("ISSUETRACKER;SELECT;TYPE;" + event.getMember().getId())) {
            this.questionType = event.getValues().get(0);
            event.deferEdit().queue();
            event.getMessage().delete().queue();
            nextQuestion();
        }
    }

    public void sendTypeSelection() {
        this.timer.cancel();
        SelectionMenu.Builder menu = SelectionMenu.create("ISSUETRACKER;SELECT;TYPE;" + member.getId())
                .setPlaceholder(this.server.getServer().translate(this.server.getPlugin(), "issuetracker_choose-type-placeholder"))
                .setRequiredRange(1, 1);

        for (String type : this.server.getQuestionTypes()) {
            menu.addOption(type, type);
        }

        MessageBuilder builder = new MessageBuilder();
        builder.setContent(member.getAsMention());
        builder.setEmbeds(new EmbedBuilder()
                .setColor(LupoColor.BLUE.getColor())
                .setAuthor(member.getUser().getAsTag() + " (" + member.getId() + ")", null, member.getUser().getAvatarUrl())
                .setDescription(this.server.getServer().translate(this.server.getPlugin(), "issuetracker_choose-type"))
                .build());

        this.channel.sendMessage(builder.build()).setActionRow(menu.build()).queue();
        this.startTimeOutTimer();
    }

    public void startTimeOutTimer() {
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!IssueCreator.this.server.getCreators().containsKey(IssueCreator.this.member.getIdLong())) {
                    this.cancel();
                    return;
                }
                IssueCreator.this.channel.sendMessage(IssueCreator.this.server.getServer().translate(IssueCreator.this.server.getPlugin(), "issuetracker_no-response",
                        IssueCreator.this.member.getAsMention())).queue();
                IssueCreator.this.server.getCreators().remove(IssueCreator.this.member.getIdLong());
            }
        }, 120*1000L);
    }
}
