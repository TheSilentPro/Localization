package tsp.localization.api.loader;

import tsp.localization.api.Language;

import java.io.IOException;
import java.util.Map;

/**
 * Language loader for {@link tsp.localization.api.Localization}.
 *
 * @author TheSilentPro (Silent)
 */
public interface LanguageLoader<T> {

    Map<String, Language<T>> load() throws IOException;

}