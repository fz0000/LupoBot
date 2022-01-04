package de.nickkel.lupobot.core.plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.nickkel.lupobot.core.LupoBot;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class PluginHelper {

    private final File file;

    public PluginHelper(File file) {
        this.file = file;
    }

    public String loadFile(String fileName) {
        try {
            JarFile jf = new JarFile(this.file);
            JarEntry je = jf.getJarEntry(fileName);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(jf.getInputStream(je)))) {
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    builder.append(line);
                }
                jf.close();
                br.close();
                return builder.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public JsonObject loadJson(String filename) {
        String file = this.loadFile(filename);
        if (file == null) {
            return null;
        }
        return new JsonParser().parse(file).getAsJsonObject();
    }

    public Class<?> loadClass(String name) {
        try {
            return Class.forName(name, true, getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public URLClassLoader getClassLoader() {
        try {
            return URLClassLoader.newInstance(new URL[]{ new URL("jar:file:" + file.toPath() +"!/") });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public URLClassLoader getPluginClassLoader() {
        try {
            return new URLClassLoader(new URL[]{file.toURL()}, Thread.currentThread().getContextClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Class<?> findClass(String name) {
        try {
            URLClassLoader child  = new URLClassLoader(new URL[] {new URL("file:" + this.file.toString())}, LupoBot.class.getClassLoader());
            return Class.forName(name, true, child);
        } catch (MalformedURLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Class<?> findClassWithJarEntry(String name) {
        try {
            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> e = jarFile.entries();

            URL[] urls = {new URL("jar:file:" + file.getAbsolutePath())};
            URLClassLoader cl = URLClassLoader.newInstance(urls);

            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if(je.isDirectory() || !je.getName().endsWith(".class")){
                    continue;
                }
                if (je.getName().contains(name)){
                    String className = je.getName().substring(0,je.getName().length()-6);
                    className = className.replace('/', '.');
                    return cl.loadClass(className);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
        return null;
    }
}