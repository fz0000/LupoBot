package de.nickkel.lupobot.core.tasks;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class SaveDataTask extends TimerTask {
    @Override
    public void run() {
        List<LupoServer> servers = new ArrayList<>(LupoBot.getInstance().getSaveQueuedServers());
        List<LupoUser> users = new ArrayList<>(LupoBot.getInstance().getSaveQueuedUsers());
        LupoBot.getInstance().getSaveQueuedServers().clear();
        LupoBot.getInstance().getSaveQueuedUsers().clear();
        int serverAmount = 0;
        LupoBot.getInstance().getLogger().info("Trying to save data of queued servers ...");
        for (LupoServer server : servers) {
            serverAmount++;
            server.saveData();
        }
        LupoBot.getInstance().getLogger().info("Saved data of " + serverAmount + " servers!");

        int userAmount = 0;
        LupoBot.getInstance().getLogger().info("Trying to save data of queued users ...");
        for (LupoUser user : users) {
            userAmount++;
            user.saveData();
        }
        LupoBot.getInstance().getLogger().info("Saved data of " + userAmount + " users!");

        LupoBot.getInstance().getLogger().info("Trying to save bot data ...");
        LupoBot.getInstance().saveData();
        LupoBot.getInstance().getLogger().info("Saved bot data!");
    }
}
