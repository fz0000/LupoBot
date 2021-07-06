package de.nickkel.lupobot.launcher;

import lombok.Getter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

public class LupoLauncher {

    @Getter
    private LupoLauncher instance;

    public static void main(String[] args) {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("client.encoding.override", "UTF-8");

        // ascii graphic
        System.out.println();
        System.out.println(" _                          ____          _   ");
        System.out.println("| |    _   _  _ __    ___  | __ )   ___  | |_ ");
        System.out.println("| |   | | | || '_ \\  / _ \\ |  _ \\  / _ \\ | __|");
        System.out.println("| |___| |_| || |_) || (_) || |_) || (_) || |_ ");
        System.out.println("|_____|\\__,_|| .__/  \\___/ |____/  \\___/  \\__|");
        System.out.println("             |_|");
        System.out.println();

        System.out.println("Starting launcher ...");
        List<String> files = Arrays.asList("configs", "plugins");
        for (String path : files) {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Could not find " + path + " file! Created file");
                file.mkdirs();
            }
        }
        new LupoLauncher().run(args);
    }

    public void run(String[] args) {
        this.instance = this;
        System.out.println("Starting LupoBot ...");

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(3));
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to start LupoBot!", e);
        }
        startBot(args);
    }

    public void startBot(String[] args) {
        Path path = null;
        for (File file : new File(".").listFiles()) {
            if (file.getName().endsWith(".jar") && file.getName().contains("core")) { //should be the core
                path = file.toPath();
            }
        }

        if (path == null) {
            throw new NullPointerException("Could not find core in default directory!");
        }

        try (JarFile jarFile = new JarFile(path.toFile())) {
            URLClassLoader classLoader = new LauncherClassLoader(new URL[]{path.toUri().toURL()});
            Thread.currentThread().setContextClassLoader(classLoader);
            String mainClass = jarFile.getManifest().getMainAttributes().getValue("Main-Class");
            Method main = classLoader.loadClass(mainClass).getMethod("main", String[].class);
            main.invoke(null, (Object) args);
        } catch (IOException | ReflectiveOperationException e) {
            throw new RuntimeException("Failed to start LupoBot!", e);
        }
    }
}
