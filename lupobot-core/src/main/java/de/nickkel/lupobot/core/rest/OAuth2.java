package de.nickkel.lupobot.core.rest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.jagrosh.jdautilities.oauth2.OAuth2Client;
import com.jagrosh.jdautilities.oauth2.Scope;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2Guild;
import com.jagrosh.jdautilities.oauth2.entities.OAuth2User;
import com.jagrosh.jdautilities.oauth2.session.Session;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.util.TimeUtils;
import lombok.Getter;
import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.List;

public class OAuth2 {

    @Getter
    private final OAuth2Client client;

    public OAuth2() {
        this.client = new OAuth2Client.Builder()
                .setClientSecret(LupoBot.getInstance().getConfig().getString("clientSecret"))
                .setClientId(LupoBot.getInstance().getConfig().getLong("clientId"))
                .build();
    }

    public String getUser(String redirect, String code) {
        Session session;
        OAuth2User user;
        try {
            session = this.client.startSession(code, this.client.getStateController().generateNewState(redirect), "", Scope.IDENTIFY, Scope.GUILDS).complete();
            user = this.client.getUser(session).complete();

            if (user != null) {
                JsonObject guilds = new JsonObject();
                for (OAuth2Guild guild : this.client.getGuilds(user.getSession()).complete()) {
                    List<String> permissions = new ArrayList<>();
                    for (Permission permission : guild.getPermissions()) {
                        permissions.add(permission.toString());
                    }
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("id", guild.getIdLong());
                    jsonObject.addProperty("name", guild.getName());
                    jsonObject.addProperty("iconUrl", guild.getIconUrl());
                    jsonObject.add("permissions", new Gson().toJsonTree(permissions));
                    jsonObject.addProperty("permissionsRaw", guild.getPermissionsRaw());
                    jsonObject.addProperty("timeCreated", TimeUtils.format(guild.getTimeCreated()));
                    guilds.add(guild.getId(), jsonObject);
                }

                Document document = new Document();
                document.append("id", user.getIdLong());
                document.append("name", user.getName());
                document.append("discriminator", user.getDiscriminator());
                document.append("asMention", user.getAsMention());
                document.append("avatarUrl", user.getAvatarUrl());
                document.append("guilds", guilds);

                LupoUser lupoUser = LupoUser.getById(user.getIdLong());
                lupoUser.getData().append("cookiesToken", code);

                return document.convertToJson();
            }
        } catch (Exception e) {
            return e.getMessage();
        }

        return "User not found";
    }
}
