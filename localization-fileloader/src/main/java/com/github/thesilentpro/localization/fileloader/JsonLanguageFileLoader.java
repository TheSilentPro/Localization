package com.github.thesilentpro.localization.fileloader;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.github.thesilentpro.localization.api.Language;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author TheSilentPro (Silent)
 */
public class JsonLanguageFileLoader<T> extends LanguageFileLoader<T> {

    private Gson gson;

    public JsonLanguageFileLoader(Class<?> clazzLoader, String resourcesPath, File container) {
        super(clazzLoader, resourcesPath, container);
    }

    @Override
    public boolean isValid(File file) {
        return file.getName().endsWith(".json");
    }

    @Override
    public Optional<Language<T>> load(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File is null or does not exist");
        }
        if (this.gson == null) {
            this.gson = new Gson();
        }
        try (FileReader reader = new FileReader(file)) {
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> loadedMessages = gson.fromJson(reader, mapType);

            if (loadedMessages != null) {
                Map<String, T> flattenedMessages = new HashMap<>();
                flattenMessages(loadedMessages, "", flattenedMessages);

                return Optional.of(new Language<>(resolveLanguageName(file.getName()), flattenedMessages));
            } else {
                return Optional.empty();
            }
        }
    }

    public void setGson(Gson gson) {
        this.gson = gson;
    }

    public Gson getGson() {
        return gson;
    }

}
