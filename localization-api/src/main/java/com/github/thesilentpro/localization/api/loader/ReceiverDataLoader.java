package com.github.thesilentpro.localization.api.loader;

import com.github.thesilentpro.localization.api.Localization;

import java.util.Map;

/**
 * Receiver data loader/saver for {@link Localization}.
 *
 * @param <T> Receiver type
 * @author TheSilentPro (Silent)
 */
public interface ReceiverDataLoader<T> {

    Map<T, String> load();

    void save(Map<T, String> data);

}