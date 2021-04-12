package de.nickkel.lupobot.core.controller;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import io.javalin.Javalin;
import io.javalin.http.Context;

import static io.javalin.apibuilder.ApiBuilder.*;

public class ServerController {

    public ServerController(Javalin app) {
        app.routes(() -> {
            path("v1/servers", () -> {
                get(this::getServers);
                path("total", () -> {
                    get(this::getTotalServers);
                });
                path(":id", () -> {
                    get(this::getServer);
                    post(this::editServer);
                });
            });
        });
    }

    public void getTotalServers(Context ctx) {
        ctx.result(new Document().append("totalServers", LupoBot.getInstance().getShardManager().getGuilds().size()).convertToJson());
    }

    public void editServer(Context ctx) {
        String key, value;
        if (ctx.queryParam("key") != null) {
            key = ctx.queryParam("key");
        } else {
            ctx.status(404).result("Query param key is missing");
            return;
        }
        if (ctx.queryParam("value") != null) {
            value = ctx.queryParam("value");
        } else {
            ctx.status(404).result("Query param value is missing");
            return;
        }

        LupoPlugin plugin = null;
        if (ctx.queryParam("plugin") != null) {
            plugin = LupoBot.getInstance().getPlugin(ctx.queryParam("plugin"));
        }
        LupoServer server = LupoServer.getById(Long.parseLong(ctx.pathParam("id")));
        if (server == null) {
            ctx.status(404).result("Server not found");
            return;
        }

        if (key.equals("_id")) {
            ctx.status(423).result("The key cannot be _id");
            return;
        }

        if (plugin == null) {
            for (LupoPlugin all : LupoBot.getInstance().getPlugins()) {
                if (all.getInfo().name().equals(key)) {
                    ctx.status(404).result("Edit plugin data is only possibly with the query param plugin");
                    return;
                }
            }

            if (server.getData().containsKey(key)) {
                try {
                    server.getData().append(key, Integer.parseInt(value));
                    server.getData().append(key, Long.parseLong(value));
                } catch (NumberFormatException e) {
                    server.getData().append(key, value);
                }
                ctx.status(201);
            }
        } else {
            BasicDBObject dbObject = (BasicDBObject) server.getData().get(plugin.getInfo().name());
            if (dbObject != null && dbObject.containsKey(key)) {
                try {
                    server.appendPluginData(plugin, key, Integer.parseInt(value));
                    server.appendPluginData(plugin, key, Long.parseLong(value));
                } catch (NumberFormatException e) {
                    server.appendPluginData(plugin, key, value);
                }
                ctx.status(201);
            }
        }
    }

    public void getServers(Context ctx) {
        Document document = new Document();
        for (LupoServer server : LupoBot.getInstance().getServers().values()) {
            document.append(server.getGuild().getId(), getServerObject(server));
        }
        ctx.status(201).result(document.convertToJson());
    }

    public void getServer(Context ctx) {
        if (LupoBot.getInstance().getShardManager().getGuildById(ctx.pathParam("key")) == null) {
            ctx.status(404).result("Server not found");
        } else {
            Document document = new Document(getServerObject(LupoServer.getById(Long.parseLong(ctx.pathParam("id")))));
            ctx.status(201).result(document.convertToJson());
        }
    }


    private JsonObject getServerObject(LupoServer server) {
        return new Document().loadJsonFromString(server.getData().toJson()).getJsonObject();
    }
}
