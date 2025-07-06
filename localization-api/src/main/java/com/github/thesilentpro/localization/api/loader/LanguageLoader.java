package com.github.thesilentpro.localization.api.loader;

import com.github.thesilentpro.localization.api.Localization;
import com.github.thesilentpro.localization.api.Language;

import java.io.IOException;
import java.util.Map;

/**
 * Language loader for {@link Localization}.
 *
 * @author TheSilentPro (Silent)
 */
public interface LanguageLoader<T> {

    Map<String, Language<T>> load() throws IOException;

}