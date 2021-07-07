package de.nickkel.lupobot.core.rest;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.controller.*;
import io.javalin.Javalin;
import lombok.Getter;
public class RestServer {

    @Getter
    private Javalin app;
    @Getter
    private final OAuth2 oAuth2 = new OAuth2();

    public RestServer(int port) {
        if (port == -1) {
            LupoBot.getInstance().getLogger().error("Could not start RestServer! Please enter a valid port in the config.json");
            return;
        }

        this.app = Javalin.create(config -> {
            config.enableCorsForAllOrigins();
            config.accessManager((handler, ctx, permittedRoles) -> {
                if (ctx.host().contains("localhost")) {
                    handler.handle(ctx);
                } else {
                    ctx.status(401).result("Unauthorized");
                }
            });
        }).start(port);

        new PluginController(this.app);
        new ServerController(this.app);
        new UserController(this.app);
        new ShardController(this.app);
        new GuildController(this.app);
        new BotController(this.app);
        new OAuth2Controller(this.app);
        new CommandController(this.app);
    }
}
