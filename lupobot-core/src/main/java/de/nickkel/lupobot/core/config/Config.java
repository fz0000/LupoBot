package de.nickkel.lupobot.core.config;

import com.google.gson.*;
import de.nickkel.lupobot.core.LupoBot;
import lombok.Getter;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Config implements iConfig {

    public static Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();
    public static final JsonParser PARSER = new JsonParser();

    @Getter
    private JsonObject jsonObject;
    private File file;

    public Config() { }

    public Config(InputStream inputStream) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = null;
        try {
            jsonObject = (JsonObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
            this.jsonObject = jsonObject;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public Config(File file) {
        this.file = file;
    }

    public Config(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public Config(File file, JsonObject jsonObject) {
        this.file = file;
        this.jsonObject = jsonObject;
    }

    @Override
    public Config append(String key, String value) {
        if(value == null)
            return this;
        this.jsonObject.addProperty(key, value);
        return this;
    }

    @Override
    public Config append(String key, Number value) {
        if(value == null)
            return this;
        this.jsonObject.addProperty(key, value);
        return this;
    }

    @Override
    public Config append(String key, Integer value) {
        if(value == null)
            return this;
        this.jsonObject.addProperty(key, value);
        return this;
    }

    @Override
    public Config append(String key, Boolean value) {
        if(value == null)
            return this;
        this.jsonObject.addProperty(key, value);
        return this;
    }

    @Override
    public Config append(String key, Long value) {
        if(value == null)
            return this;
        this.jsonObject.addProperty(key, value);
        return this;
    }

    @Override
    public Config append(String key, Object value) {
        if(value == null)
            return this;
        this.jsonObject.add(key, GSON.toJsonTree(value));
        return this;
    }

    @Override
    public Config append(String key, List<String> value) {
        if(value == null)
            return this;
        JsonArray jsonElements = new JsonArray();

        for(String b : value) {
            jsonElements.add(new JsonPrimitive(b));
        }
        this.jsonObject.add(key, jsonElements);
        return this;
    }

    @Override
    public Config append(String key, JsonElement value) {
        if(value == null)
            return this;
        this.jsonObject.add(key, value);
        return this;
    }

    public Config remove(String key) {
        this.jsonObject.remove(key);
        return this;
    }

    public String getString(String key) {
        return this.jsonObject.get(key).getAsString();
    }

    public Number getNumber(String key) {
        return this.jsonObject.get(key).getAsNumber();
    }

    public Integer getInt(String key) {
        return this.jsonObject.get(key).getAsInt();
    }

    public Boolean getBoolean(String key) {
        return this.jsonObject.get(key).getAsBoolean();
    }

    public Long getLong(String key) {
        return this.jsonObject.get(key).getAsLong();
    }

    public <T> T getObject(String key, Class<T> class_) {
        if(!this.jsonObject.has(key))
            return null;
        JsonElement jsonElement = this.jsonObject.get(key);

        return GSON.fromJson(jsonElement, class_);
    }

    public List<String> getList(String key) {
        JsonArray jsonElements = getArray(key);

        List<String> list = new ArrayList<>();

        for(short i = 0; i < jsonElements.size(); i++) {
            list.add(jsonElements.get(i).getAsString());
        }

        return list;
    }

    public JsonElement getJsonElement(String key) {
        if(!this.jsonObject.has(key))
            return null;
        return this.jsonObject.get(key);
    }

    public Boolean has(String key) {
        if(!this.jsonObject.has(key))
            return false;
        return true;
    }

    public JsonArray getArray(String key) {
        return this.jsonObject.get(key).getAsJsonArray();
    }

    public String convertToJson() {
        return GSON.toJson(jsonObject);
    }

    public String convertToJsonString() {
        return jsonObject.toString();
    }

    public Config saveAsConfig() {
        try(PrintWriter printWriter = new PrintWriter(file, "UTF-8")) {
            printWriter.write(GSON.toJson(jsonObject));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Config loadDocument() {
        try {
            this.jsonObject = (JsonObject) PARSER.parse(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
        } catch (IOException e) {
            return null;
        }
        return this;
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1)
            sb.append((char)cp);
        return sb.toString();
    }

    public Config readJsonFromUrl(String url) {
        InputStream is = null;
        try {
            is = (new URL(url)).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JsonObject json = new JsonObject().getAsJsonObject(jsonText);
            this.jsonObject = json;
            return this;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
