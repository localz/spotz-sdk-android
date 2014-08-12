package com.localz.spotz.api.utils;

import com.google.api.client.util.DateTime;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

public enum GsonHolder {

    INSTANCE;

    private final Gson gson;

    private GsonHolder() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
                    @Override
                    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return new Date(DateTime.parseRfc3339(json.getAsString()).getValue());
                    }
                })
                .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                    @Override
                    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(new DateTime(src).toStringRfc3339());
                    }
                })
                .registerTypeAdapter(Double.class, new JsonSerializer<Double>() {

                    public JsonElement serialize(Double src, Type typeOfSrc,
                                                 JsonSerializationContext context) {
                        if (src == src.intValue()) {
                            return new JsonPrimitive(src.intValue());
                        }
                        return new JsonPrimitive(src);
                    }
                })
                .create();
    }

    public Gson getGson() {
        return gson;
    }
}
