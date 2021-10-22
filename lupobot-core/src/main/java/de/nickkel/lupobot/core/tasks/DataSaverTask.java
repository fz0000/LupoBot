package de.nickkel.lupobot.core.tasks;

import de.nickkel.lupobot.core.LupoBot;
import de.nickkel.lupobot.core.data.LupoServer;
import de.nickkel.lupobot.core.data.LupoUser;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class DataSaverTask extends TimerTask {

    private int counter = 0;

    @Override
    public void run() {
        List<LupoServer> servers = new ArrayList<>(LupoBot.getInstance().getSaveQueuedServers());
        List<LupoUser> users = new ArrayList<>(LupoBot.getInstance().getSaveQueuedUsers());

        LupoBot.getInstance().getSaveQueuedServers().clear();
        LupoBot.getInstance().getSaveQueuedUsers().clear();

        LupoBot.getInstance().getLogger().info(" ");
        LupoBot.getInstance().getLogger().info("============== Data Saver is running ==============");
        LupoBot.getInstance().getLogger().info("Queued servers: " + servers.size() + " | Queued users: " + users.size());
        LupoBot.getInstance().getLogger().info(" ");

        // Server data
        AtomicInteger serverAmount = new AtomicInteger(0);
        for (LupoServer server : servers) {
            server.saveData();
            serverAmount.getAndIncrement();
            this.counter++;
            printProgress("servers", servers.size());
        }
        LupoBot.getInstance().getLogger().info("Saved " + serverAmount + " server data");
        this.counter = 0;

        // User data
        AtomicInteger userAmount = new AtomicInteger(0);
        for (LupoUser user : users) {
            user.saveData();
            userAmount.getAndIncrement();
            this.counter++;
            printProgress("users", users.size());
        }
        LupoBot.getInstance().getLogger().info("Saved " + userAmount + " user data");

        // Bot data
        LupoBot.getInstance().saveData();
        LupoBot.getInstance().getLogger().info("Saved bot data");

        LupoBot.getInstance().getLogger().info("===================================================");
        LupoBot.getInstance().getLogger().info(" ");
    }

    private void printProgress(String type, int size) {
        if (size % 10 == 0) {
            double percentage = (this.counter / size) * 100.0D;
            LupoBot.getInstance().getLogger().info(("Saving progress of "  + type + ": " + percentage + "%"));
        }
    }
}
