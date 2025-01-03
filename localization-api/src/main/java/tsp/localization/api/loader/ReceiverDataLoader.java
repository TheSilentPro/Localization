package tsp.localization.api.loader;

import java.util.Map;

/**
 * Receiver data loader/saver for {@link tsp.localization.api.Localization}.
 *
 * @param <T> Receiver type
 * @author TheSilentPro (Silent)
 */
public interface ReceiverDataLoader<T> {

    Map<T, String> load();

    void save(Map<T, String> data);

}