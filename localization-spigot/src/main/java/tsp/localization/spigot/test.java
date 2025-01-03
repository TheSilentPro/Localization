package tsp.localization.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import tsp.localization.api.ConsoleLogLevel;
import tsp.localization.fileloader.YamlLanguageFileLoader;

import java.io.File;
import java.io.IOException;

/**
 * @author TheSilentPro (Silent)
 */
public class test extends JavaPlugin {

    @Override
    public void onEnable() {
        SpigotLocalization localization = new SpigotLocalization(this);
        try {
            localization.loadLanguages(new YamlLanguageFileLoader<>(getClass(), "messages", new File(getDataFolder(), "messages")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        localization.sendConsoleMessage("test");
        localization.sendConsoleMessage(ConsoleLogLevel.ERROR, "nested.message", msg -> msg.replace("HELLO", ""));
    }

}
