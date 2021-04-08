package de.nickkel.lupobot.core.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.util.TimeUtils;
import io.javalin.Javalin;
import io.javalin.http.Context;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.List;

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
                path("members/:member", () -> {
                    get(this::getMember);
                });
            });
        });
    }

    public void getMember(Context ctx) {
        Guild guild = LupoBot.getInstance().getShardManager().getGuildById(ctx.pathParam("id"));
        if (guild == null) {
            ctx.status(404).result("Guild not found");
        } else {
            Member member;
            try {
                member = guild.getMemberById(ctx.pathParam("member"));
                if (member == null) {
                    ctx.status(404).result("Member not found");
                    return;
                }
            } catch (NumberFormatException e) {
                ctx.status(404).result("Member not found");
                return;
            }

            List<Long> roles = new ArrayList<>();
            for (Role role : member.getRoles()) {
                roles.add(role.getIdLong());
            }
            List<String> permissions = new ArrayList<>();
            for (Permission permission : member.getPermissions()) {
                permissions.add(permission.toString());
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", member.getIdLong());
            jsonObject.addProperty("effectiveName", member.getEffectiveName());
            jsonObject.addProperty("nickname", member.getNickname());
            jsonObject.addProperty("owner", member.isOwner());
            jsonObject.addProperty("avatarUrl", member.getUser().getAvatarUrl());
            jsonObject.addProperty("timeCreated", TimeUtils.format(member.getTimeCreated()));
            jsonObject.addProperty("timeJoined", TimeUtils.format(member.getTimeJoined()));
            jsonObject.addProperty("color", member.getColorRaw());
            jsonObject.add("roles", new Gson().toJsonTree(roles));
            jsonObject.add("permissions", new Gson().toJsonTree(permissions));
            ctx.result(new Document(jsonObject).convertToJson());
        }
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
