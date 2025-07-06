package com.github.thesilentpro.localization.fileloader;

import org.yaml.snakeyaml.Yaml;
import com.github.thesilentpro.localization.api.Language;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author TheSilentPro (Silent)
 */
public class YamlLanguageFileLoader<T> extends LanguageFileLoader<T> {

    private Yaml yaml;

    public YamlLanguageFileLoader(Class<?> clazzLoader, String resourcesPath, File container) {
        super(clazzLoader, resourcesPath, container);
    }

    @Override
    public boolean isValid(File file) {
        return file.getName().endsWith(".yml") || file.getName().endsWith(".yaml");
    }

    @Override
    public Optional<Language<T>> load(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File is null or does not exist");
        }
        if (this.yaml == null) {
            this.yaml = new Yaml();
        }
        try (FileInputStream fis = new FileInputStream(file)) {
            Map<String, Object> loadedMessages = yaml.loadAs(fis, Map.class);

            if (loadedMessages != null) {
                Map<String, T> flattenedMessages = new HashMap<>();
                flattenMessages(loadedMessages, "", flattenedMessages);

                return Optional.of(new Language<>(resolveLanguageName(file.getName()), flattenedMessages));
            } else {
                return Optional.empty();
            }
        }
    }

    public void setYaml(Yaml yaml) {
        this.yaml = yaml;
    }

    public Yaml getYaml() {
        return yaml;
    }

}