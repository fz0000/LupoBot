package de.nickkel.lupobot.plugin.leveling.commands;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.util.LupoColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.*;

@CommandInfo(name = "ranking", aliases = {"ranklist"}, category = "general", cooldown = 20)
public class RankingCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        Guild guild = context.getGuild();

        Map<Long, Long> users = new HashMap<>();
        BasicDBObject data = (BasicDBObject) context.getServer().getPluginData(context.getPlugin(), "xp");

        for (String id : data.keySet()) {
            BasicDBObject dbObject = (BasicDBObject) data.get(id);
            users.put(Long.valueOf(id), dbObject.getLong("xp"));
        }

        LinkedHashMap<Long, Long> sortedUsers = new LinkedHashMap<>();
        users.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> sortedUsers.put(x.getKey(), x.getValue()));

        String userNames = "", xp = "";
        int rank = 1;
        for (Long id : sortedUsers.keySet()) {
            if (rank == 20) {
                break;
            }
            LupoUser user = LupoUser.getById(id);
            userNames = userNames + rank + ". " + user.getAsMention() + "\n";
            xp = xp + context.getServer().formatLong(sortedUsers.get(id)) + "\n";
            rank++;
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getTime());
        builder.setColor(LupoColor.BLUE.getColor());
        builder.setAuthor(guild.getName() + " (" + guild.getIdLong() + ")", null, guild.getIconUrl());
        builder.addField(context.getServer().translate(context.getPlugin(), "leveling_ranking-name"), userNames, true);
        builder.addField(context.getServer().translate(context.getPlugin(), "leveling_ranking-xp"), xp, true);
        send(context, builder);
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}