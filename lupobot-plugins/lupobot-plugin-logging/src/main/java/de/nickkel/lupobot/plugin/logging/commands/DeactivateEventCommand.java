package de.nickkel.lupobot.plugin.logging.commands;

import de.nickkel.lupobot.core.command.CommandContext;
import de.nickkel.lupobot.core.command.CommandInfo;
import de.nickkel.lupobot.core.command.LupoCommand;
import net.dv8tion.jda.api.Permission;

@CommandInfo(name = "deactivateevent", aliases = "de", category = "general", permissions = Permission.ADMINISTRATOR)
public class DeactivateEventCommand extends LupoCommand {

    @Override
    public void onCommand(CommandContext context) {

    }
}
