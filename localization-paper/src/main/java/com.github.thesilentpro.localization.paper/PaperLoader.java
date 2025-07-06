package com.github.thesilentpro.localization.paper;

import net.kyori.adventure.text.Component;
import com.github.thesilentpro.localization.fileloader.YamlLanguageFileLoader;

import java.io.File;

/**
 * Implementation of a custom {@link YamlLanguageFileLoader} for the paper platform.
 *
 * @author TheSilentPro (Silent)
 */
public class PaperLoader extends YamlLanguageFileLoader<Component> {

    public PaperLoader(Class<?> clazzLoader, String resourcesPath, File container) {
        super(clazzLoader, resourcesPath, container);
    }

    @Override
    public Component mapObject(Object object) {
        return switch (object) {
            case String str -> Component.text(str);
            case Boolean b -> Component.text(b);
            case Integer n -> Component.text(n);
            case Double n -> Component.text(n);
            case Long n -> Component.text(n);
            case Float n -> Component.text(n);
            case Character n -> Component.text(n);
            case null, default -> throw new IllegalArgumentException("Unsupported object: " + object);
        };
    }

}
