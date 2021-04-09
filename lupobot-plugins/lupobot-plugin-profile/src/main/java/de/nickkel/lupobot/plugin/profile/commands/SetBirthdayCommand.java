package de.nickkel.lupobot.plugin.profile.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.profile.LupoProfilePlugin;
import de.nickkel.lupobot.plugin.profile.data.Profile;
import net.dv8tion.jda.api.EmbedBuilder;

@CommandInfo(name = "setbirthday", category = "general")
public class SetBirthdayCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        if (context.getArgs().length == 1) {
            Profile profile = LupoProfilePlugin.getInstance().getProfile(context.getMember());
            String birthday = context.getArgs()[0];
            if (profile.isValidDate(birthday)) {
                profile.setBirthday(birthday);
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(LupoColor.GREEN.getColor())
                        .setAuthor(context.getMember().getUser().getAsTag() + " (" + context.getMember().getId() + ")", null,
                         context.getMember().getUser().getAvatarUrl())
                        .setTimestamp(context.getMessage().getTimeCreated())
                        .setDescription(context.getServer().translate(context.getPlugin(), "profile_setbirthday-success", birthday));
                context.getChannel().sendMessage(builder.build()).queue();
            } else {
                sendSyntaxError(context, "profile_setbirthday-invalid-date");
            }
        } else {
            sendHelp(context);
        }
    }
}
