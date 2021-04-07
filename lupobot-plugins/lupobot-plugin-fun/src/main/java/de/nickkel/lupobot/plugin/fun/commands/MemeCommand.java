package de.nickkel.lupobot.plugin.fun.commands;

import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;

import java.io.IOException;

@CommandInfo(name = "meme", category = "general", cooldown = 5)
public class MemeCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        try {
            Document document = new Document(("https://apis.duncte123.me/meme?nsfw=false"));
            if (!document.getBoolean("success")) {
                failure(context);
            } else {
                JsonObject jsonObject = document.getJsonElement("data").getAsJsonObject();
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(LupoColor.ORANGE.getColor());
                builder.setTitle(jsonObject.get("title").getAsString(), jsonObject.get("url").getAsString());
                builder.setImage(jsonObject.get("image").getAsString());
                builder.setFooter(context.getServer().translate(context.getPlugin(), "fun_meme-footer"));
                context.getChannel().sendMessage(builder.build()).queue();
            }
        } catch (Exception e) {
            failure(context);
        }

    }

    private void failure(CommandContext context) {
        context.getChannel().sendMessage(context.getServer().translate(context.getPlugin(), "fun_api-requests")).queue();
    }
}
