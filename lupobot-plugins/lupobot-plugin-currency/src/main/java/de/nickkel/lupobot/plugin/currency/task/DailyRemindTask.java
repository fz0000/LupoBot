package de.nickkel.lupobot.plugin.currency.task;

import com.mongodb.BasicDBList;
import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoUser;
import de.nickkel.lupobot.core.plugin.LupoPlugin;
import de.nickkel.lupobot.core.util.TimeUtils;
import de.nickkel.lupobot.plugin.currency.LupoCurrencyPlugin;

import java.util.TimerTask;

public class DailyRemindTask extends TimerTask {
    @Override
    public void run() {
        LupoPlugin plugin = LupoBot.getInstance().getPlugin(LupoCurrencyPlugin.getInstance().getInfo().name());
        BasicDBList dbList = (BasicDBList) LupoBot.getInstance().getPluginData(plugin, "dailyReminds");
        for (int i=0; i < dbList.size(); i++) {
            LupoUser user = LupoUser.getById((long) dbList.get(i));
            long lastDailyCoins = user.getPluginLong(plugin, "lastDailyCoins");
            String lastDailyRemind = (String) user.getPluginData(plugin, "lastDailyRemind");
            if (lastDailyCoins == -1 || lastDailyCoins+86400000-System.currentTimeMillis() < 0) {
                if (lastDailyRemind == null || !lastDailyRemind.equals(TimeUtils.currentDate())) {
                    LupoBot.getInstance().getShardManager().retrieveUserById(user.getId()).complete().openPrivateChannel().queue(success -> {
                        success.sendMessage("Don't forget to pick up your daily reward! :coin:").queue();
                    });
                    user.appendPluginData(plugin, "lastDailyRemind", TimeUtils.currentDate());
                }
            }
        }
        LupoBot.getInstance().getLogger().info("Successfully daily reminded users");
    }
}
