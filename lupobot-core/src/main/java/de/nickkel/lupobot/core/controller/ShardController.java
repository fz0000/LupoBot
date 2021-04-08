package de.nickkel.lupobot.core.controller;

import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import io.javalin.Javalin;
import io.javalin.http.Context;
import net.dv8tion.jda.api.JDA;
import static io.javalin.apibuilder.ApiBuilder.*;

public class ShardController {

    public ShardController(Javalin app) {
        app.routes(() -> {
            path("v1/shards", () -> {
                get(this::getShards);
                path(":id", () -> {
                    get(this::getShard);
                    post(this::editShard);
                });
            });
        });
    }

    public void getShards(Context ctx) {
        Document document = new Document();
        for (JDA shard : LupoBot.getInstance().getShardManager().getShards()) {
            document.append(String.valueOf(shard.getShardInfo().getShardId()), getShardObject(String.valueOf(shard.getShardInfo().getShardId())));
        }
        ctx.status(201).result(document.convertToJson());
    }

    public void getShard(Context ctx) {
        if (LupoBot.getInstance().getShardManager().getShardById(ctx.pathParam("id")) == null) {
            ctx.status(404).result("Not found");
        } else {
            Document document = new Document(getShardObject(ctx.pathParam("id")));
            ctx.status(201).result(document.convertToJson());
        }
    }

    public void editShard(Context ctx) {
        String type = ctx.queryParam("type");
        if (LupoBot.getInstance().getShardManager().getShardById(ctx.pathParam("id")) == null) {
            ctx.status(404).result("Not found");
        } else {
            JDA shard = LupoBot.getInstance().getShardManager().getShardById(ctx.pathParam("id"));
            if (type != null) {
                if (type.equals("shutdown")) {
                    shard.shutdown();
                } else {
                    LupoBot.getInstance().getShardManager().restart(shard.getShardInfo().getShardId());
                }
                ctx.status(201);
            } else {
                ctx.status(400).result("Query param type is null");
            }
        }
    }

    private JsonObject getShardObject(String id) {
        JDA shard = LupoBot.getInstance().getShardManager().getShardById(id);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", shard.getShardInfo().getShardId());
        jsonObject.addProperty("total", shard.getShardInfo().getShardTotal());
        jsonObject.addProperty("ping", shard.getGatewayPing());
        jsonObject.addProperty("guilds", shard.getGuilds().size());
        jsonObject.addProperty("status", shard.getStatus().name());
        return jsonObject;
    }
}
