package com.github.thesilentpro.localization.fileloader.data;

import com.github.thesilentpro.localization.api.loader.ReceiverDataLoader;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Abstract implementation of a {@link ReceiverDataLoader} that uses {@link Properties} and {@link File} to persist data.
 *
 * @author TheSilentPro (Silent)
 */
public abstract class ReceiverDataFileLoader<T> implements ReceiverDataLoader<T> {

    private final File file;

    public ReceiverDataFileLoader(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    public String serialize(T object) {
        return object.toString();
    }

    public T deserialize(Object object) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public Map<T, String> load() {
        Properties properties = new Properties();
        try (FileReader reader = new FileReader(this.file)) {
            properties.load(reader);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        Map<T, String> result = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            result.put(deserialize(entry.getKey()), entry.getValue().toString());
        }

        return result;
    }

    @Override
    public void save(Map<T, String> data) {
        Properties properties = new Properties();
        for (Map.Entry<T, String> entry : data.entrySet()) {
            properties.setProperty(serialize(entry.getKey()), entry.getValue());
        }
        try (FileWriter writer = new FileWriter(this.file)) {
            properties.store(writer, null);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}