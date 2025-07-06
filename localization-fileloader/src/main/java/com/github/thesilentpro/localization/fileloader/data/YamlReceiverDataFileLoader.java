package com.github.thesilentpro.localization.fileloader.data;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author TheSilentPro (Silent)
 */
public class YamlReceiverDataFileLoader<T> extends ReceiverDataFileLoader<T> {

    private Yaml yaml;

    public YamlReceiverDataFileLoader(File file) {
        super(file);
    }

    public void setYaml(Yaml yaml) {
        this.yaml = yaml;
    }

    public Yaml getYaml() {
        return yaml;
    }

    @Override
    public Map<T, String> load() {
        if (this.yaml == null) {
            this.yaml = new Yaml();
        }
        Map<T, String> result = new HashMap<>();
        try (FileReader reader = new FileReader(getFile())) {
            result.putAll(yaml.loadAs(reader, Map.class));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    @Override
    public void save(Map<T, String> data) {
        if (this.yaml == null) {
            this.yaml = new Yaml();
        }
        if (data == null || data.isEmpty()) {
            throw new NullPointerException("Data is null or empty!");
        }
        try (FileWriter writer = new FileWriter(getFile())) {
            yaml.dump(data, writer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
