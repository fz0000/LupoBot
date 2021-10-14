package de.nickkel.lupobot.core.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.language.Language;
import de.nickkel.lupobot.core.language.LanguageHandler;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import io.javalin.Javalin;
import io.javalin.http.Context;
import net.dv8tion.jda.api.Permission;

import java.util.ArrayList;
import java.util.List;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CommandController {

    public CommandController(Javalin app) {
        app.routes(() -> {
           path("v1/commands", () -> {
              get(this::getCommands);
              path(":name", () -> {
                  get(this::getCommand);
                  post(this::editCommand);
              });
           });
        });
    }

    public void getCommands(Context ctx) {
        Document document = new Document();
        for (LupoCommand command : LupoBot.getInstance().getCommands()) {
            document.append(command.getInfo().name(), getCommandObject(command));
        }
        ctx.status(201).result(document.convertToJson());
    }

    public void getCommand(Context ctx) {
        if (LupoBot.getInstance().getCommand(ctx.pathParam("name")) == null) {
            ctx.status(404).result("Not found");
        } else {
            Document document = new Document(getCommandObject(LupoBot.getInstance().getCommand(ctx.pathParam("name"))));
            ctx.status(201).result(document.convertToJson());
        }
    }

    public void editCommand(Context ctx) {
        if (LupoBot.getInstance().getCommand(ctx.pathParam("name")) == null) {
            ctx.status(404).result("Not found");
        } else {
            String type = ctx.queryParam("type");
            if (type != null) {
                if (type.equals("disable")) {
                    LupoBot.getInstance().getCommand(ctx.pathParam("name")).disable();
                } else {
                    LupoBot.getInstance().getCommand(ctx.pathParam("name")).enable();
                }
                ctx.status(201);
            }
        }
    }

    private JsonObject getCommandObject(LupoCommand command) {
        List<String> permissions = new ArrayList<>();
        for (Permission permission : command.getInfo().permissions()) {
            permissions.add(permission.getName());
        }

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", command.getInfo().name());
        if (command.getPlugin() != null) {
            jsonObject.addProperty("plugin", command.getPlugin().getInfo().name());
        }
        jsonObject.addProperty("category", command.getInfo().category());
        jsonObject.addProperty("cooldown", command.getInfo().cooldown());
        jsonObject.addProperty("hidden", command.getInfo().hidden());
        jsonObject.addProperty("staffPower", command.getInfo().staffPower());
        jsonObject.add("aliases", new Gson().toJsonTree(command.getInfo().aliases()));
        jsonObject.add("permissions", new Gson().toJsonTree(permissions));

        LanguageHandler handler = LupoBot.getInstance().getLanguageHandler();
        if (command.getPlugin() != null) {
            handler = command.getPlugin().getLanguageHandler();
        }
        String pluginName = "core";
        if (command.getPlugin() != null) {
            pluginName = command.getPlugin().getInfo().name();
        }

        Document translatedDescriptions = new Document();
        for (Language language : handler.getLanguages().values()) {
            translatedDescriptions.append(language.getName(), handler.translate(language.getName(), pluginName + "_" + command.getInfo().name() + "-description"));
        }
        jsonObject.add("translatedDescriptions", translatedDescriptions.getJsonObject());
        Document translatedUsages = new Document();
        for (Language language : handler.getLanguages().values()) {
            translatedUsages.append(language.getName(), handler.translate(language.getName(), pluginName + "_" + command.getInfo().name() + "-usage"));
        }
        jsonObject.add("translatedUsages", translatedUsages.getJsonObject());
        Document translatedExamples = new Document();
        for (Language language : handler.getLanguages().values()) {
            translatedExamples.append(language.getName(), handler.translate(language.getName(), pluginName + "_" + command.getInfo().name() + "-example"));
        }
        jsonObject.add("translatedExamples", translatedExamples.getJsonObject());
        return jsonObject;
    }
}
