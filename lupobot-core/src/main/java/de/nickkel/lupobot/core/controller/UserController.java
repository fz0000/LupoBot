package de.nickkel.lupobot.core.controller;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import io.javalin.Javalin;
import io.javalin.http.Context;

import static io.javalin.apibuilder.ApiBuilder.*;

public class UserController {

    public UserController(Javalin app) {
        app.routes(() -> {
            path("v1/users", () -> {
                get(this::getUsers);
                path("total", () -> {
                    get(this::getTotalUsers);
                });
                path(":id", () -> {
                    get(this::getUser);
                    post(this::editUser);
                });
            });
        });
    }

    public void getTotalUsers(Context ctx) {
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("name").getAsString());
        DBCollection collection = database.getCollection("users");
        ctx.result(new Document().append("totalUsers", collection.getCount()).convertToJson());
    }

    public void getUsers(Context ctx) {
        Document document = new Document();
        for (LupoUser user : LupoBot.getInstance().getUsers().values()) {
            document.append(String.valueOf(user.getId()), getUserObject(user));
        }
        ctx.status(201).result(document.convertToJson());
    }

    public void editUser(Context ctx) {
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

        if (LupoUser.getById(Long.parseLong(ctx.pathParam("id"))) == null) {
            ctx.status(404).result("User not found");
        }

        LupoPlugin plugin = null;
        if (ctx.queryParam("plugin") != null) {
            plugin = LupoBot.getInstance().getPlugin(ctx.queryParam("plugin"));
        }
        LupoUser user = LupoUser.getById(Long.parseLong(ctx.pathParam("id")));
        if (user == null) {
            ctx.status(404).result("User not found");
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

            if (user.getData().containsKey(key)) {
                try {
                    user.getData().append(key, Integer.parseInt(value));
                    user.getData().append(key, Long.parseLong(value));
                } catch (NumberFormatException e) {
                    user.getData().append(key, value);
                }
                ctx.status(201);
            }
        } else {
            BasicDBObject dbObject = (BasicDBObject) user.getData().get(plugin.getInfo().name());
            if (dbObject != null && dbObject.containsKey(key)) {
                try {
                    user.appendPluginData(plugin, key, Integer.parseInt(value));
                    user.appendPluginData(plugin, key, Long.parseLong(value));
                } catch (NumberFormatException e) {
                    user.appendPluginData(plugin, key, value);
                }
                ctx.status(201);
            }
        }
        user.saveData();
    }

    public void getUser(Context ctx) {
        if (LupoUser.getById(Long.parseLong(ctx.pathParam("id"))) == null) {
            ctx.status(404).result("User not found");
        } else {
            LupoUser user = LupoUser.getById(Long.parseLong(ctx.pathParam("id")));
            Document document = new Document(getUserObject(user));
            if (user.getStaffGroup().getRole() != null) {
                document.append("staffGroup", user.getStaffGroup().getRole().getName());
            }
            ctx.status(201).result(document.convertToJson());
        }
    }


    private JsonObject getUserObject(LupoUser user) {
        return new Document().loadJsonFromString(user.getData().toJson()).getJsonObject();
    }
}
