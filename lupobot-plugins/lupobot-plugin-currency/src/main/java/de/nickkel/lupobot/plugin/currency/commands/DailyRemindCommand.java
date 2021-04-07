package de.nickkel.lupobot.plugin.currency.commands;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;
import net.dv8tion.jda.api.EmbedBuilder;

@CommandInfo(name = "dailyremind", category = "reward")
public class DailyRemindCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        BasicDBObject pluginObject = (BasicDBObject) LupoBot.getInstance().getData().get(LupoCurrencyPlugin.getInstance().getInfo().name());
        BasicDBList dbList = (BasicDBList) pluginObject.get("dailyReminds");

        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                context.getMember().getUser().getAvatarUrl());
        builder.setTimestamp(context.getMessage().getTimeCreated());
        if (dbList.contains(context.getMember().getIdLong())) {
            dbList.remove(context.getMember().getIdLong());
            builder.setColor(LupoColor.RED.getColor());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_dailyremind-removed"));
        } else {
            dbList.add(context.getMember().getIdLong());
            builder.setColor(LupoColor.GREEN.getColor());
            builder.setDescription(context.getServer().translate(context.getPlugin(), "currency_dailyremind-added"));
        }
        pluginObject.append("dailyReminds", dbList);
        LupoBot.getInstance().getData().append(LupoCurrencyPlugin.getInstance().getInfo().name(), pluginObject);
        context.getChannel().sendMessage(builder.build()).queue();
    }
}