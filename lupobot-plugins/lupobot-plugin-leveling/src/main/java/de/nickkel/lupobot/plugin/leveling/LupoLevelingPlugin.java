package de.nickkel.lupobot.plugin.leveling;

import com.mongodb.BasicDBObject;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.plugin.PluginInfo;
import de.nickkel.lupobot.core.util.ListenerRegister;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@PluginInfo(name = "leveling", author = "Nickkel")
public class LupoLevelingPlugin extends LupoPlugin {

    @Getter
    public static LupoLevelingPlugin instance;
    private final Map<Long, Long> lastReceivedXP = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        this.registerCommands("de.nickkel.lupobot.plugin.leveling.commands");
        this.registerListeners("de.nickkel.lupobot.plugin.leveling.listener");
    }

    @Override
    public void onDisable() {

    }

    public boolean isReadyToReceiveXP(LupoServer server, LupoUser user) {
        if (!this.lastReceivedXP.containsKey(server.getGuild().getIdLong()+user.getId())) {
            return true;
        }
        long time = this.lastReceivedXP.get(server.getGuild().getIdLong()+user.getId())-System.currentTimeMillis();
        return time < 0;
    }

    public long getXP(LupoServer server, LupoUser user) {
        checkIfDataExists(server, user);
        BasicDBObject pluginObject = (BasicDBObject) server.getData().get(this.getInfo().name());
        BasicDBObject xpObject = (BasicDBObject) pluginObject.get("xp");

        BasicDBObject data = (BasicDBObject) xpObject.get(String.valueOf(user.getId()));
        return data.getLong("xp");
    }

    public long getLevel(LupoServer server, LupoUser user) {
        checkIfDataExists(server, user);
        BasicDBObject pluginObject = (BasicDBObject) server.getData().get(this.getInfo().name());
        BasicDBObject xpObject = (BasicDBObject) pluginObject.get("xp");

        BasicDBObject data = (BasicDBObject) xpObject.get(String.valueOf(user.getId()));
        return data.getLong("level");
    }

    public void addLevel(LupoServer server, LupoUser user) {
        checkIfDataExists(server, user);
        long level = getLevel(server, user)+1;

        BasicDBObject pluginObject = (BasicDBObject) server.getData().get(this.getInfo().name());
        BasicDBObject xpObject = (BasicDBObject) pluginObject.get("xp");

        BasicDBObject data = (BasicDBObject) xpObject.get(String.valueOf(user.getId()));
        data.append("level", level);

        pluginObject.append("xp", xpObject);
        this.lastReceivedXP.put(server.getGuild().getIdLong()+user.getId(), System.currentTimeMillis()+60000);

        BasicDBObject rewardObject = (BasicDBObject) pluginObject.get("rewardRoles");
        if (rewardObject.containsKey(String.valueOf(level))) {
            if (existsRewardRole(server, level)) {
                server.getGuild().addRoleToMember(user.getId(), server.getGuild().getRoleById(rewardObject.getLong(String.valueOf(level)))).queue();
            }
        }
    }

    public void addXP(LupoServer server, LupoUser user, long xp) {
        addXP(server, user, xp, null);
    }

    public void addXP(LupoServer server, LupoUser user, long xp, TextChannel channel) {
        checkIfDataExists(server, user);

        if (user.isBot()) {
            return;
        }


        long level = getLevel(server, user);
        while (getXP(server, user)+xp >= getRequiredXP(getLevel(server, user)+1)) {
            addLevel(server, user);
            if (channel != null) {
                Role role = null;
                BasicDBObject rewardObject = (BasicDBObject) server.getPluginData(LupoBot.getInstance().getPlugin(this.getInfo().name()), "rewardRoles");
                if (rewardObject.containsKey(String.valueOf(level))) {
                    if (existsRewardRole(server, level)) {
                        role = server.getGuild().getRoleById(rewardObject.getLong(String.valueOf(level)));
                    }
                }

                if (server.getPluginData(LupoBot.getInstance().getPlugin(this.getInfo().name()), "levelUpMessage") == null) {
                    String roleName = "/";
                    if (role != null) {
                        roleName = role.getName();
                    }
                    channel.sendMessage(server.translate(LupoBot.getInstance().getPlugin(this.getInfo().name()), "leveling_level-up-" + (role != null),
                            user.getAsMention(), getLevel(server, user), roleName)).queue();
                } else {
                    String message = (String) server.getPluginData(LupoBot.getInstance().getPlugin(this.getInfo().name()), "levelUpMessage");
                    message.replace("%member%", user.getAsMention()).replace("%level%", String.valueOf(getLevel(server, user)));
                    if (role != null) {
                        message = message.replace("%role%", role.getName());
                    } else {
                        message = message.replace("%role%", "/");
                    }
                    channel.sendMessage(message).queue();
                }
            }
        }

        BasicDBObject pluginObject = (BasicDBObject) server.getData().get(this.getInfo().name());
        BasicDBObject xpObject = (BasicDBObject) pluginObject.get("xp");

        BasicDBObject data = (BasicDBObject) xpObject.get(String.valueOf(user.getId()));
        data.append("xp", getXP(server, user)+xp);

        pluginObject.append("xp", xpObject);
        this.lastReceivedXP.put(server.getGuild().getIdLong()+user.getId(), System.currentTimeMillis()+60000);
    }

    public boolean existsRewardRole(LupoServer server, long level) {
        BasicDBObject pluginObject = (BasicDBObject) server.getData().get(LupoLevelingPlugin.getInstance().getInfo().name());
        BasicDBObject rewardObject = (BasicDBObject) pluginObject.get("rewardRoles");
        if (rewardObject.containsKey(String.valueOf(level))) {
            if (server.getGuild().getRoleById(rewardObject.getLong(String.valueOf(level))) == null) {
                rewardObject.remove(String.valueOf(level));
                pluginObject.append("rewardRoles", rewardObject);
                return false;
            }
        }
        return true;
    }

    public void checkIfDataExists(LupoServer server, LupoUser user) {
        BasicDBObject pluginObject = (BasicDBObject) server.getData().get(this.getInfo().name());
        BasicDBObject xpObject = (BasicDBObject) pluginObject.get("xp");

        if (!xpObject.containsKey(String.valueOf(user.getId()))) {
            xpObject.append(String.valueOf(user.getId()), new BasicDBObject().append("level", 0).append("xp", 0));
            pluginObject.append("xp", xpObject);
        }
    }

    public long getRequiredXP(long level) {
        return Math.round(0.83333333333 * level * (2 * Math.pow(level, 2) + 27 * level + 91));
    }

    public long getRandomXP() {
        return new Random().nextInt((25-15)+1) + 15;
    }
}
