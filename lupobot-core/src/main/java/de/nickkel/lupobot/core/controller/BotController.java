package de.nickkel.lupobot.core.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.util.TimeUtils;
import io.javalin.Javalin;
import io.javalin.http.Context;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import static io.javalin.apibuilder.ApiBuilder.*;

public class BotController {

    public BotController(Javalin app) {
        app.routes(() -> {
            path("v1/bot", () -> {
                get(this::getBot);
                path("status", () -> {
                    get(this::getStatus);
                    path(":key", () -> {
                        post(this::setStatus);
                    });
                });
                path("activity-name", () -> {
                    get(this::getActivityName);
                    path(":name", () -> {
                        post(this::setActivityName);
                    });
                });
            });
        });
    }

    public void getBot(Context ctx) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", LupoBot.getInstance().getSelfUser().getIdLong());
        jsonObject.addProperty("ping", LupoBot.getInstance().getShardManager().getAverageGatewayPing());
        jsonObject.addProperty("runtime", TimeUtils.format(System.currentTimeMillis()-LupoBot.getInstance().getStartMillis()));
        jsonObject.addProperty("version", LupoBot.getInstance().getClass().getPackage().getImplementationVersion());
        jsonObject.addProperty("status", LupoBot.getInstance().getSelfUser().getJDA().getPresence().getStatus().toString());
        jsonObject.addProperty("activityName", LupoBot.getInstance().getSelfUser().getJDA().getPresence().getActivity().getName());
        jsonObject.addProperty("activityType", LupoBot.getInstance().getSelfUser().getJDA().getPresence().getActivity().getType().name());
        jsonObject.add("languages", new Gson().toJsonTree(LupoBot.getInstance().getAvailableLanguages()));

        ctx.status(201).result(new Document(jsonObject).convertToJson());
    }

    public void getStatus(Context ctx) {
        ctx.status(201).result(new Document().append("status", LupoBot.getInstance().getSelfUser().getJDA().getPresence().getStatus().toString()).convertToJson());
    }

    public void setStatus(Context ctx) {
        OnlineStatus status = OnlineStatus.fromKey(ctx.pathParam("key"));
        if (status == OnlineStatus.UNKNOWN) return;
        LupoBot.getInstance().getShardManager().setStatus(OnlineStatus.fromKey(ctx.pathParam("key")));
    }

    public void getActivityName(Context ctx) {
        ctx.status(201).result(new Document().append("activityName", LupoBot.getInstance().getSelfUser().getJDA().getPresence().getActivity().getName()).convertToJson());
    }

    public void setActivityName(Context ctx) {
        LupoBot.getInstance().getShardManager().setActivity(Activity.of(LupoBot.getInstance().getSelfUser().getJDA().getPresence().getActivity().getType(), ctx.pathParam("name")));
    }
}
