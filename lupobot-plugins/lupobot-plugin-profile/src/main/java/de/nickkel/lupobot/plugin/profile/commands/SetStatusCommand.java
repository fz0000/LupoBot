package de.nickkel.lupobot.plugin.profile.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.command.SlashOption;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.profile.LupoProfilePlugin;
import de.nickkel.lupobot.plugin.profile.data.Profile;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

@CommandInfo(name = "setstatus", category = "general")
@SlashOption(name = "status", type = OptionType.STRING)
public class SetStatusCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgsAsString().length() <= 30) {
            Profile profile = LupoProfilePlugin.getInstance().getProfile(context.getMember());
            String newStatus;
            if (context.getSlash() == null) {
                newStatus = context.getArgsAsString();
            } else {
                newStatus = context.getSlash().getOption("status").getAsString();
            }

            if (newStatus.length() == 0) {
                newStatus = "/";
            }
            String oldStatus = profile.getStatus();
            if (oldStatus.length() == 0) {
                oldStatus = "/";
            }
            
            profile.setStatus(context.getArgsAsString());
            EmbedBuilder builder = new EmbedBuilder()
                    .setColor(LupoColor.GREEN.getColor())
                    .setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                            context.getMember().getUser().getAvatarUrl())
                    .setTimestamp(context.getTime())
                    .setDescription(context.getServer().translate(context.getPlugin(), "profile_setstatus-success"))
                    .addField(context.getServer().translate(context.getPlugin(), "profile_setstatus-old"), oldStatus, false)
                    .addField(context.getServer().translate(context.getPlugin(), "profile_setstatus-new"), newStatus, false);
            send(context, builder);
        } else {
            sendSyntaxError(context, "profile_setstatus-too-long");
        }
    }

    @Override
    public void onSlashCommand(CommandContext context, SlashCommandEvent slash) {
        onCommand(context);
    }
}
