package com.github.thesilentpro.localization.fileloader.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author TheSilentPro (Silent)
 */
public class JsonReceiverDataFileLoader<T> extends ReceiverDataFileLoader<T> {

    private Gson gson;

    public JsonReceiverDataFileLoader(File file) {
        super(file);
    }

    @Override
    public Map<T, String> load() {
        if (gson == null) {
            gson = new Gson();
        }
        Map<T, String> result = new HashMap<>();
        try (FileReader reader = new FileReader(getFile())) {
            Type mapType = new TypeToken<Map<T, String>>() {}.getType();
            result = gson.fromJson(reader, mapType);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }


    @Override
    public void save(Map<T, String> data) {
        if (gson == null) {
            gson = new Gson();
        }
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("Data is null or empty!");
        }
        try (FileWriter writer = new FileWriter(getFile())) {
            gson.toJson(data, writer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}