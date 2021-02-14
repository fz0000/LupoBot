package de.nickkel.lupobot.plugin.fun.commands;

import com.google.gson.JsonObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.config.Document;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;

@CommandInfo(name = "meme", category = "general", cooldown = 5)
public class MemeCommand extends LupoCommand {
    @Override
    public void onCommand(CommandContext context) {
        Document config = new Document(("https://apis.duncte123.me/meme?nsfw=false"));
        if(!config.getBoolean("success")) {
            context.getChannel().sendMessage(context.getServer().translate(context.getPlugin(), "fun_meme-requests")).queue();
            LupoBot.getInstance().getLogger().error("Error using meme command: " + config.convertToJsonString());
        } else {
            JsonObject jsonObject = config.getJsonElement("data").getAsJsonObject();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(LupoColor.ORANGE.getColor());
            builder.setTitle(jsonObject.get("title").getAsString(), jsonObject.get("url").getAsString());
            builder.setImage(jsonObject.get("image").getAsString());
            builder.setFooter(context.getServer().translate(context.getPlugin(), "fun_meme-footer"));
            context.getChannel().sendMessage(builder.build()).queue();
        }
    }
}
