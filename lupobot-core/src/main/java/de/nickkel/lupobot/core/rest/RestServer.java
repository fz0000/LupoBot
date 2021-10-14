package de.nickkel.lupobot.core.rest;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.controller.*;
import io.javalin.Javalin;
import lombok.Getter;

import java.io.File;
import java.util.List;

public class RestServer {

    @Getter
    private Javalin app;
    @Getter
    private final OAuth2 oAuth2 = new OAuth2();
    @Getter
    private Document config;
    @Getter
    private boolean started;

    public RestServer() {
        try {
            this.start();
        } catch (Exception e) {
            LupoBot.getInstance().getLogger().error("Could not start RestServer!", e);
        }
    }

    public void start() {
        this.config = new Document(new File("configs/rest-server.json")).loadDocument();
        int port = this.config.getInt("port");
        List<String> ipWhitelist = this.config.getList("ipWhitelist");

        this.app = Javalin.create(config -> {
            config.enableCorsForAllOrigins();
            config.accessManager((handler, ctx, permittedRoles) -> {
                if (ctx.host().startsWith("localhost") || ipWhitelist.contains(ctx.ip())) {
                    LupoBot.getInstance().getLogger().info("Received API request from " + ctx.ip() + ": " + ctx.path());
                    handler.handle(ctx);
                } else {
                    ctx.status(401).result("Access denied");
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

        this.started = true;
    }

    public void stop() {
        this.app.stop();
        this.started = false;
    }
}
