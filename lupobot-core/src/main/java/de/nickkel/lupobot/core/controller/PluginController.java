package de.nickkel.lupobot.core.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class PluginController {

    public PluginController(Javalin app) {
        app.routes(() -> {
           path("v1/plugins", () -> {
              get(this::getPlugins);
              path(":name", () -> {
                  get(this::getPlugin);
              });
           });
        });
    }

    public void getPlugins(Context ctx) {
        Document document = new Document();
        for (LupoPlugin plugin : LupoBot.getInstance().getPlugins()) {
            document.append(plugin.getInfo().name(), getPluginObject(plugin));
        }
        ctx.status(201).result(document.convertToJson());
    }

    public void getPlugin(Context ctx) {
        if (LupoBot.getInstance().getPlugin(ctx.pathParam("name")) == null) {
            ctx.status(404).result("Not found");
        } else {
            Document document = new Document(getPluginObject(LupoBot.getInstance().getPlugin(ctx.pathParam("name"))));
            ctx.status(201).result(document.convertToJson());
        }
    }


    private JsonObject getPluginObject(LupoPlugin plugin) {
        List<String> commands = new ArrayList<>();
        for(LupoCommand command : plugin.getCommands()) {
            commands.add(command.getInfo().name());
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", plugin.getInfo().name());
        jsonObject.addProperty("author", plugin.getInfo().author());
        jsonObject.addProperty("version", plugin.getInfo().version());
        jsonObject.add("commands", new Gson().toJsonTree(commands));
        jsonObject.add("guildWhitelist", new Gson().toJsonTree(plugin.getInfo().guildWhitelist()));
        return jsonObject;
    }
}
