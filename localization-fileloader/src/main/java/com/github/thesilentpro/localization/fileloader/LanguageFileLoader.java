package com.github.thesilentpro.localization.fileloader;

import com.github.thesilentpro.localization.api.Language;
import com.github.thesilentpro.localization.api.loader.LanguageLoader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author TheSilentPro (Silent)
 */
public abstract class LanguageFileLoader<T> implements LanguageLoader<T> {

    private final Class<?> clazzLoader;
    private final String resourcesPath;
    private final File container;

    public LanguageFileLoader(Class<?> clazzLoader, String resourcesPath, File container) {
        this.clazzLoader = clazzLoader;
        this.resourcesPath = resourcesPath;
        this.container = container;
    }

    public abstract Optional<Language<T>> load(File file) throws IOException;

    @Override
    public Map<String, Language<T>> load() throws IOException {
        createDefaults();

        File[] files = container.listFiles();
        if (files == null) {
            throw new NullPointerException("Files list is null! Ensure that the container is a directory.");
        }

        Map<String, Language<T>> result = new HashMap<>();
        for (File file : files) {
            if (!isValid(file)) {
                continue;
            }

            result.put(resolveLanguageName(file.getName()), load(file).orElseThrow(() -> new RuntimeException("Failed to load language data for: " + file.getName())));
        }

        return result;
    }

    public boolean isValid(File file) {
        return true;
    }

    public String resolveLanguageName(String name) {
        return name.substring(0, name.lastIndexOf("."));
    }

    public T mapObject(Object object) {
        return (T) object;
    }

    protected void flattenMessages(Map<String, Object> currentMap, String parentKey, Map<String, T> flattenedMap) {
        for (Map.Entry<String, Object> entry : currentMap.entrySet()) {
            String newKey = parentKey.isEmpty() ? entry.getKey() : parentKey + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                flattenMessages(nestedMap, newKey, flattenedMap);
            } else {
                flattenedMap.put(newKey, mapObject(value));
            }
        }
    }

    /**
     * Create the default language files from your /resources folder.
     */
    public void createDefaults() {
        URL url = clazzLoader.getClassLoader().getResource(resourcesPath);
        if (url == null) {
            throw new NullPointerException("No resource!");
        }

        if (!container.exists()) {
            //noinspection ResultOfMethodCallIgnored
            container.mkdir();
        }

        // This is required otherwise Files.walk will throw FileSystem Exception.
        try {
            String[] array = url.toURI().toString().split("!");
            FileSystem fs = FileSystems.newFileSystem(URI.create(array[0]), new HashMap<>());

            //noinspection resource
            Files.walk(Paths.get(url.toURI()))
                    .forEach(path -> {
                        try {
                            File out = new File(container.getAbsolutePath() + "/" + path.getFileName());
                            // If file is not of YAML type or if it already exists, ignore it.
                            if (isValid(out) && !out.exists()) {
                                Files.copy(path, out.toPath());
                            }
                        } catch (IOException ex) {
                            //noinspection CallToPrintStackTrace
                            ex.printStackTrace();
                        }
                    });

            fs.close();
        } catch (URISyntaxException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public File getContainer() {
        return container;
    }

    public String getResourcesPath() {
        return resourcesPath;
    }

    public Class<?> getClazzLoader() {
        return clazzLoader;
    }

}