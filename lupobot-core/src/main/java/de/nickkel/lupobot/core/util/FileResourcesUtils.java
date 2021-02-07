package de.nickkel.lupobot.core.util;

import de.nickkel.lupobot.core.LupoBot;
import lombok.Getter;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;

public class FileResourcesUtils {

    @Getter
    private final Class clazz;

    public FileResourcesUtils(Class clazz) {
        this.clazz = clazz;
    }

    // get a file from the resources folder
    // works everywhere, IDEA, unit test and JAR file.
    public InputStream getFileFromResourceAsStream(String fileName) {

        // The class loader that loaded the class
        ClassLoader classLoader = this.clazz.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }

    public void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[128];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }

    }

    // Get all paths from a folder that inside the JAR file
    public List<Path> getPathsFromResourceJAR(String folder)
            throws URISyntaxException, IOException {

        List<Path> result;

        // get path of the current running JAR
        String jarPath = this.clazz.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();

        // file walks JAR
        URI uri = URI.create("jar:file:" + jarPath);
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            result = Files.walk(fs.getPath(folder))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }

        return result;

    }

    // print input stream
    public Properties getPropertiesByInputStream(InputStream is) {

        try (InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            Properties properties = new Properties();


            while ((line = reader.readLine()) != null) {
                try {
                    if(!line.isEmpty()) {
                        properties.setProperty(line.split(" = ")[0], line.split(" = ")[1]);
                    }
                } catch(ArrayIndexOutOfBoundsException e) {
                    LupoBot.getInstance().getLogger().error("Failed to load locale file of " + clazz.getName() + " due to line split fail (" + line + ")");
                }

            }

            return properties;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}