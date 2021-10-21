package de.nickkel.lupobot.plugin.fun.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.io.IOException;

@CommandInfo(name = "meme", category = "general", cooldown = 5)
public class MemeCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        try {
            Document document = new Document("https://meme-api.herokuapp.com/gimme/10");
            JsonArray jsonArray = document.getJsonElement("memes").getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {
                if (!jsonArray.get(i).getAsJsonObject().get("nsfw").getAsBoolean()) {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(LupoColor.ORANGE.getColor());
                    builder.setTitle(jsonArray.get(i).getAsJsonObject().get("title").getAsString(), jsonArray.get(i).getAsJsonObject().get("postLink").getAsString());
                    builder.setImage(jsonArray.get(i).getAsJsonObject().get("url").getAsString());
                    builder.setFooter(context.getServer().translate(context.getPlugin(), "fun_meme-footer"));
                    send(context, builder);
                    break;
                }
            }
        } catch (Exception e) {
            failure(context);
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }

    private void failure(CommandContext context) {
        send(context, context.getServer().translate(context.getPlugin(), "fun_api-requests"));
    }
}
