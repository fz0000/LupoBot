package de.nickkel.lupobot.plugin.currency.commands;

import com.mongodb.*;
import com.mongodb.client.model.Sorts;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@CommandInfo(name = "richest", category = "general", cooldown = 30)
public class RichestCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        Guild guild = context.getGuild();

        Map<Long, Long> users = new HashMap<>();
        DB database = LupoBot.getInstance().getMongoClient().getDB(LupoBot.getInstance().getConfig().getJsonElement("database")
                .getAsJsonObject().get("database").getAsString());
        DBCollection collection = database.getCollection("users");
        DBCursor cursor = collection.find().limit(20); // TODO: descending order by coins

        while(cursor.hasNext()) {
            DBObject dbObject = cursor.next();
            BasicDBObject pluginObject = (BasicDBObject) dbObject.get("currency");
            users.put((long) dbObject.get("_id"), pluginObject.getLong("coins"));
        }

        LinkedHashMap<Long, Long> sortedUsers = new LinkedHashMap<>();
        users.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedUsers.put(x.getKey(), x.getValue()));

        String userNames = "", coins = "";
        int rank = 1;
        for(Long id : sortedUsers.keySet()) {
            if(rank == 20) {
                break;
            }
            User user = LupoBot.getInstance().getShardManager().getUserById(id);
            userNames = userNames + rank + ". " + user.getAsMention() + "\n";
            coins = coins + context.getServer().formatLong(sortedUsers.get(id)) + "\n";
            rank++;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setAuthor(context.getServer().translate(context.getPlugin(), "currency_richest-title"), null, LupoBot.getInstance().getSelfUser().getAvatarUrl());
        builder.addField(context.getServer().translate(context.getPlugin(), "currency_richest-name"), userNames, true);
        builder.addField(context.getServer().translate(context.getPlugin(), "currency_richest-coins"), coins, true);
        context.getChannel().sendMessage(builder.build()).queue();
    }
}