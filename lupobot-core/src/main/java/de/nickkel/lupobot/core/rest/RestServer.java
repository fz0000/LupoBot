package de.nickkel.lupobot.core.rest;

import de.nickkel.lupobot.core.controller.PluginController;
import io.javalin.Javalin;
import lombok.Getter;
public class RestServer {

    @Getter
    private final Javalin app;

    public RestServer(int port) {
        this.app = Javalin.create(config -> {
            config.accessManager((handler, ctx, permittedRoles) -> {
                if (ctx.host().contains("localhost")) {
                    handler.handle(ctx);
                } else {
                    ctx.status(401).result("Unauthorized");
                }
            });
        }).start(port);

        new PluginController(this.app);
    }
}
