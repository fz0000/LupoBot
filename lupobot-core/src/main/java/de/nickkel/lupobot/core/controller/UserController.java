package de.nickkel.lupobot.core.controller;

import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.data.LupoUser;
import io.javalin.Javalin;
import io.javalin.http.Context;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

public class UserController {

    public UserController(Javalin app) {
        app.routes(() -> {
            path("v1/users", () -> {
                get(this::getUsers);
                path(":id", () -> {
                    get(this::getUser);
                });
            });
        });
    }

    public void getUsers(Context ctx) {
        Document document = new Document();
        for (LupoUser user : LupoBot.getInstance().getUsers().values()) {
            document.append(String.valueOf(user.getId()), getUserObject(user));
        }
        ctx.status(201).result(document.convertToJson());
    }

    public void getUser(Context ctx) {
        if (LupoUser.getById(Long.parseLong(ctx.pathParam("id"))) == null) {
            ctx.status(404).result("Not found");
        } else {
            Document document = new Document(getUserObject(LupoUser.getById(Long.parseLong(ctx.pathParam("id")))));
            ctx.status(201).result(document.convertToJson());
        }
    }


    private JsonObject getUserObject(LupoUser user) {
        return new Document().loadJsonFromString(user.getData().toJson()).getJsonObject();
    }
}
