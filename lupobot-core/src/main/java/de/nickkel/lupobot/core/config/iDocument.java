package de.nickkel.lupobot.core.config;

import com.google.gson.JsonElement;

import java.util.List;

public interface iDocument {

    Document append(String key, String value);

    Document append(String key, Number value);

    Document append(String key, Integer value);

    Document append(String key, Boolean value);

    Document append(String key, Long value);

    Document append(String key, Object value);

    Document append(String key, List<String> value);

    Document append(String key, JsonElement value);

}