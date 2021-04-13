package de.nickkel.lupobot.core.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.util.TimeUtils;
import io.javalin.Javalin;
import io.javalin.http.Context;

import static io.javalin.apibuilder.ApiBuilder.*;

public class BotController {

    public BotController(Javalin app) {
        app.routes(() -> {
            path("v1/bot", () -> {
                get(this::getBot);
            });
        });
    }

    public void getBot(Context ctx) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", LupoBot.getInstance().getSelfUser().getIdLong());
        jsonObject.addProperty("ping", LupoBot.getInstance().getShardManager().getAverageGatewayPing());
        jsonObject.addProperty("runtime", TimeUtils.format(System.currentTimeMillis()-LupoBot.getInstance().getStartMillis()));
        jsonObject.addProperty("version", LupoBot.getInstance().getClass().getPackage().getImplementationVersion());
        jsonObject.add("languages", new Gson().toJsonTree(LupoBot.getInstance().getAvailableLanguages()));

        ctx.status(201).result(new Document(jsonObject).convertToJson());
    }
}
