package de.nickkel.lupobot.core.controller;

import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import io.javalin.Javalin;
import io.javalin.http.Context;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import static io.javalin.apibuilder.ApiBuilder.*;

public class GuildController {

    public GuildController(Javalin app) {
        app.routes(() -> {
            path("v1/guilds/:id", () -> {
                path("messages", () -> {
                    post(this::createMessage);
                });
                path("channels", () -> {
                    get(this::getChannels);
                });
            });
        });
    }

    public void getChannels(Context ctx) {
        Guild guild = LupoBot.getInstance().getShardManager().getGuildById(ctx.pathParam("id"));
        if (guild == null) {
            ctx.status(404).result("Guild not found");
        } else {
            JsonObject jsonObject = new JsonObject();
            for (GuildChannel channel : guild.getChannels()) {
                JsonObject channelObject = new JsonObject();
                channelObject.addProperty("type", channel.getType().toString());
                channelObject.addProperty("id", channel.getIdLong());
                channelObject.addProperty("name", channel.getName());
                channelObject.addProperty("position", channel.getPositionRaw());
                if (channel.getParent() != null) {
                    channelObject.addProperty("categoryName", channel.getParent().getName());
                    channelObject.addProperty("categoryId", channel.getParent().getIdLong());
                } else {
                    channelObject.addProperty("categoryName", "NONE");
                    channelObject.addProperty("categoryId", "NONE");
                }
                jsonObject.add(channel.getId(), channelObject);
            }
            ctx.result(new Document(jsonObject).convertToJson());
        }
    }

    public void createMessage(Context ctx) {
        Guild guild = LupoBot.getInstance().getShardManager().getGuildById(ctx.pathParam("id"));
        if (guild == null) {
            ctx.status(404).result("Guild not found");
            return;
        }

        if (ctx.queryParam("channelId") == null) {
            ctx.status(404).result("Query param channelId is null");
            return;
        }

        TextChannel channel = guild.getTextChannelById(ctx.queryParam("channelId"));
        if (channel == null) {
            ctx.status(404).result("Channel not found");
            return;
        }

        MessageBuilder messageBuilder = new MessageBuilder();
        if (ctx.queryParam("content") != null) {
            messageBuilder.setContent(ctx.queryParam("content"));
        }
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (ctx.queryParam("description") != null) {
            embedBuilder.setDescription(ctx.queryParam("description"));
        }
        if (ctx.queryParam("thumbnail") != null) {
            embedBuilder.setThumbnail(ctx.queryParam("thumbnail"));
        }
        if (ctx.queryParam("authorName") != null) {
            embedBuilder.setAuthor(ctx.queryParam("authorName"));
            if (ctx.queryParam("authorIconUrl") != null) {
                embedBuilder.setAuthor(ctx.queryParam("authorName"), ctx.queryParam("authorUrl"), ctx.queryParam("authorIconUrl"));
            }
        }
        if (ctx.queryParam("title") != null) {
            embedBuilder.setTitle(ctx.queryParam("title"));
            if (ctx.queryParam("title") != null && ctx.queryParam("titleUrl") != null) {
                embedBuilder.setTitle(ctx.queryParam("title"), ctx.queryParam("titleUrl"));
            }
        }
        if (ctx.queryParam("footer") != null) {
            embedBuilder.setFooter(ctx.queryParam("footer"));
            if (ctx.queryParam("footer") != null && ctx.queryParam("footerIconUrl") != null) {
                embedBuilder.setFooter(ctx.queryParam("footer"), ctx.queryParam("footerIconUrl"));
            }
        }
        if (ctx.queryParam("image") != null) {
            embedBuilder.setImage(ctx.queryParam("image"));
        }
        if (ctx.queryParam("color") != null) {
            try {
                embedBuilder.setColor(Integer.parseInt(ctx.queryParam("color")));
            } catch (NullPointerException ignored) {
            }
        }

        if (!embedBuilder.isEmpty()) {
            messageBuilder.setEmbed(embedBuilder.build());
        }

        if (ctx.queryParam("messageid") != null) {
            channel.editMessageById(ctx.queryParam("messageId"), messageBuilder.build()).queue();
        } else {
            channel.sendMessage(messageBuilder.build()).queue();
        }
    }
}
