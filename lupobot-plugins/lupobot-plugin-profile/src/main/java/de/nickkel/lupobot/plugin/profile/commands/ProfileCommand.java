package de.nickkel.lupobot.plugin.profile.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import de.nickkel.lupobot.core.util.LupoColor;
import de.nickkel.lupobot.plugin.profile.LupoProfilePlugin;
import de.nickkel.lupobot.plugin.profile.data.Profile;
import de.nickkel.lupobot.plugin.profile.enums.Badge;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;

@CommandInfo(name = "profile", category = "general")
public class ProfileCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {
        Member member = context.getMember();
        if (context.getArgs().length == 1) {
            member = context.getServer().getMember(context.getArgs()[0]);
            if (member == null) {
                sendSyntaxError(context, "profile_profile-user-not-found");
                return;
            }
        }

        Profile profile = LupoProfilePlugin.getInstance().getProfile(member);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTimestamp(context.getMessage().getTimeCreated().toInstant());
        builder.setColor(LupoColor.ORANGE.getColor());
        builder.setTitle(member.getUser().getAsTag() + " (" + member.getIdLong() + ")");
        builder.setThumbnail(member.getUser().getAvatarUrl());
        builder.setDescription(profile.getStatus());

        if (profile.getBirthday() != null) {
            builder.addField(context.getServer().translate(context.getPlugin(), "profile_profile-birthday"), profile.getBirthday(), true);
            builder.addField(context.getServer().translate(context.getPlugin(), "profile_profile-age"), String.valueOf(profile.getAge()), true);
        }
        if (profile.getGender() != null) {
            builder.addField(context.getServer().translate(context.getPlugin(), "profile_profile-gender"),
                    context.getServer().translate(context.getPlugin(), profile.getGender().getLocale()), false);
        }
        if (profile.getBadges().size() != 0) {
            String badges = "";
            if (profile.getBadges().size() >= 3) {
                for (Badge badge : profile.getBadges()) {
                    badges = badges + badge.getEmoji() + " " + badge.getTranslatedName(context.getServer()) + "\n";
                }
            } else {
                for (Badge badge : profile.getBadges()) {
                    badges = badges + badge.getEmoji() + " " + badge.getTranslatedName(context.getServer()) + ", ";
                }
                badges = badges.substring(0, badges.length() - 2);
            }
            builder.addField(context.getServer().translate(context.getPlugin(), "profile_profile-badges"), badges, false);
        }

        context.getChannel().sendMessage(builder.build()).queue();
    }
}