package de.nickkel.lupobot.core.controller;

import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.data.LupoServer;
import io.javalin.Javalin;
import io.javalin.http.Context;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class ServerController {

    public ServerController(Javalin app) {
        app.routes(() -> {
            path("v1/servers", () -> {
                get(this::getServers);
                path(":id", () -> {
                    get(this::getServer);
                });
            });
        });
    }

    public void getServers(Context ctx) {
        Document document = new Document();
        for (LupoServer server : LupoBot.getInstance().getServers().values()) {
            document.append(server.getGuild().getId(), getServerObject(server));
        }
        ctx.status(201).result(document.convertToJson());
    }

    public void getServer(Context ctx) {
        if (LupoServer.getById(Long.parseLong(ctx.pathParam("id"))) == null) {
            ctx.status(404).result("Not found");
        } else {
            Document document = new Document(getServerObject(LupoServer.getById(Long.parseLong(ctx.pathParam("id")))));
            ctx.status(201).result(document.convertToJson());
        }
    }


    private JsonObject getServerObject(LupoServer server) {
        return new Document().loadJsonFromString(server.getData().toJson()).getJsonObject();
    }
}
