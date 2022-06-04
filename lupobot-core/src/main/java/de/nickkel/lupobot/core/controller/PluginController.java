package de.nickkel.lupobot.core.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.language.Language;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import io.javalin.Javalin;
import io.javalin.http.Context;
import net.dv8tion.jda.api.JDA;

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
                  post(this::editPlugin);
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

    public void editPlugin(Context ctx) {
        String type = ctx.queryParam("type");
        if (type != null) {
            LupoPlugin plugin = LupoBot.getInstance().getPlugin(ctx.pathParam("name"));
            if (type.equals("unload")) {
                LupoBot.getInstance().getPluginLoader().unloadPlugin(plugin);
            } else {
                LupoBot.getInstance().getPluginLoader().reloadPlugin(plugin);
            }
            ctx.status(201);
        } else {
            ctx.status(404).result("Not found");
        }
    }

    private JsonObject getPluginObject(LupoPlugin plugin) {
        List<String> commands = new ArrayList<>();
        for (LupoCommand command : plugin.getCommands()) {
            commands.add(command.getInfo().name());
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", plugin.getInfo().name());
        jsonObject.addProperty("author", plugin.getInfo().author());
        jsonObject.addProperty("version", plugin.getInfo().version());
        jsonObject.addProperty("hidden", plugin.getInfo().hidden());
        jsonObject.add("commands", new Gson().toJsonTree(commands));
        jsonObject.add("guildWhitelist", new Gson().toJsonTree(plugin.getGuildWhitelist()));
        Document translatedNames = new Document();
        for (Language language : plugin.getLanguageHandler().getLanguages().values()) {
            translatedNames.append(language.getName(), plugin.getLanguageHandler().translate(language.getName(), plugin.getInfo().name() + "_display-name"));
        }
        jsonObject.add("translatedNames", translatedNames.getJsonObject());
        Document translatedDescriptions = new Document();
        for (Language language : plugin.getLanguageHandler().getLanguages().values()) {
            translatedDescriptions.append(language.getName(), plugin.getLanguageHandler().translate(language.getName(), plugin.getInfo().name() + "_description"));
        }
        jsonObject.add("translatedDescriptions", translatedDescriptions.getJsonObject());
        return jsonObject;
    }
}
