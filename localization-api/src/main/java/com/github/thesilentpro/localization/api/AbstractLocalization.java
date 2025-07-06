package com.github.thesilentpro.localization.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.github.thesilentpro.localization.api.loader.LanguageLoader;
import com.github.thesilentpro.localization.api.loader.ReceiverDataLoader;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

/**
 * An abstract implementation of {@link Localization}.
 *
 * @param <T> Message return type
 * @param <A> Argument type
 * @param <R> Receiver type
 * @author TheSilentPro (Silent)
 */
@SuppressWarnings("unchecked")
public abstract class AbstractLocalization<T, A, R> implements Localization<T, A, R> {

    private final String defaultLanguage;
    private final Map<String, Language<T>> languages; // Lang ID, Language data

    private final Map<R, String> data; // Receiver ID, Lang

    private String consoleLanguage;
    private BiConsumer<ConsoleLogLevel, T> consoleLogFunction = (level, message) -> System.out.println("[" + level.name() + "]: " + message);

    /**
     * Creates a new {@link Localization} instance.
     *
     * @param defaultLanguage The default language. Default: en
     */
    public AbstractLocalization(@Nullable String defaultLanguage) {
        defaultLanguage = defaultLanguage != null ? defaultLanguage : "en";

        this.defaultLanguage = defaultLanguage;
        this.consoleLanguage = defaultLanguage;
        this.languages = new HashMap<>();
        this.data = new HashMap<>();
    }

    public AbstractLocalization() {
        this(null);
    }

    /**
     * Retrieve a message by the receiver's language and the key.
     *
     * @param receiver The receiver.
     * @param key The message key.
     * @return If present, the message, otherwise an empty {@link Optional}
     */
    @Override
    @NotNull
    public Optional<T> getMessage(@NotNull R receiver, @NotNull String key) {
        notNull(receiver, "Receiver must not be null!");
        notNull(key, "Key must not be null!");

        Language<T> language = languages.get(data.getOrDefault(receiver, defaultLanguage));
        if (language == null) {
            return Optional.empty();
        }

        Map<String, T> messages = language.getMessages();
        if (messages == null) {
            return Optional.empty();
        }

        T message = messages.get(key);
        if (message == null) {
            // Message not specified in language data, attempt to find it in the main one.
            messages = languages.get(defaultLanguage).getMessages();
            message = messages.get(key);
        }

        return Optional.ofNullable(message);
    }

    @Override
    public abstract void sendTranslatedMessage(@NotNull R receiver, @NotNull T message);

    @Override
    public abstract void sendMessage(@NotNull R receiver, @NotNull String key, @Nullable UnaryOperator<T> function, @Nullable A... args);

    @Override
    public void sendMessage(@NotNull R receiver, @NotNull String key, @Nullable UnaryOperator<T> function) {
        sendMessage(receiver, key, function, (A[]) null);
    }

    @Override
    public void sendMessage(@NotNull R receiver, @NotNull String key, @Nullable A... args) {
        sendMessage(receiver, key, null, args);
    }

    @Override
    public void sendMessage(@NotNull R receiver, @NotNull String key) {
        sendMessage(receiver, key, null, (A[]) null);
    }

    @Override
    public void sendMessages(@NotNull String key, @NotNull R... receivers) {
        for (R receiver : receivers) {
            sendMessage(receiver, key);
        }
    }

    // Console

    @Override
    public void sendTranslatedConsoleMessage(@NotNull ConsoleLogLevel level, @NotNull T message) {
        notNull(level, "Level must not be null!");
        notNull(message, "Message must not be null!");
        consoleLogFunction.accept(level, message);
    }

    @Override
    public abstract void sendConsoleMessage(ConsoleLogLevel level, @NotNull String key, @Nullable UnaryOperator<T> function, @Nullable A... args);

    public void sendConsoleMessage(@NotNull String key, @Nullable UnaryOperator<T> function, @Nullable A... args) {
        sendConsoleMessage(ConsoleLogLevel.INFO, key, function, args);
    }

    @Override
    public void sendConsoleMessage(@Nullable ConsoleLogLevel level, @NotNull String key, @Nullable UnaryOperator<T> function) {
        sendConsoleMessage(level, key, function, (A[]) null);
    }

    public void sendConsoleMessage(@NotNull String key, @Nullable UnaryOperator<T> function) {
        sendConsoleMessage(ConsoleLogLevel.INFO, key, function);
    }

    @Override
    public void sendConsoleMessage(@Nullable ConsoleLogLevel level, @NotNull String key, @Nullable A... args) {
        sendConsoleMessage(level, key, null, args);
    }

    public void sendConsoleMessage(@NotNull String key, @Nullable A... args) {
        sendConsoleMessage(ConsoleLogLevel.INFO, key, args);
    }

    @Override
    public void sendConsoleMessage(@Nullable ConsoleLogLevel level, @NotNull String key) {
        sendConsoleMessage(level, key, null, (A[]) null);
    }

    @Override
    public void sendConsoleMessage(@NotNull String key) {
        sendConsoleMessage(ConsoleLogLevel.INFO, key);
    }

    @Override
    public @NotNull Optional<T> getConsoleMessage(@NotNull String key) {
        notNull(key, "Key must not be null!");

        Map<String, T> messages = languages.get(consoleLanguage != null ? consoleLanguage : defaultLanguage).getMessages();
        if (messages == null) {
            return Optional.empty();
        }

        T message = messages.get(key);
        if (message == null) {
            // Message not specified in language file, attempt to find it in the main one.
            messages = languages.get(defaultLanguage).getMessages();
            message = messages.get(key);
        }

        return Optional.ofNullable(message);
    }

    @Override
    public void setConsoleLogFunction(@NotNull BiConsumer<ConsoleLogLevel, T> consoleLogFunction) {
        notNull(consoleLogFunction, "Console log function can not be null!");
        this.consoleLogFunction = consoleLogFunction;
    }

    // Loaders

    /**
     * Load all languages.
     *
     * @return Number of files loaded.
     */
    public int loadLanguages(@NotNull LanguageLoader<T> loader) throws IOException {
        this.languages.putAll(loader.load());
        return this.languages.size();
    }

    /**
     * Load the receiver languages.
     */
    @Override
    public int loadReceiverData(@NotNull ReceiverDataLoader<R> loader) {
        this.data.putAll(loader.load());
        return this.data.size();
    }

    /**
     * Save the receiver languages.
     */
    @Override
    public void saveReceiverData(@NotNull ReceiverDataLoader<R> loader) {
        loader.save(this.data);
    }

    /**
     * Retrieve a <bold>mutable</bold> {@link Map} containing all language/message data.
     *
     * @return The language/message data. Format: ID, Data
     */
    @Override
    @NotNull
    public Map<String, Language<T>> getLanguages() {
        return this.languages;
    }

    /**
     * Retrieve the default language.
     *
     * @return The default language.
     */
    @Override
    @NotNull
    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    /**
     * Retrieve an <bold>immutable</bold> {@link Map} of all loaded language settings for each receiver.
     *
     * @return The language map. Format: Receiver, Language ID
     * @see #setLanguage(Object, String)
     * @see #removeLanguage(Object)
     */
    @Override
    @NotNull
    public Map<R, String> getReceiverData() {
        return Collections.unmodifiableMap(this.data);
    }

    /**
     * Retrieve the language for a specific receiver.
     *
     * @return Optional language.
     */
    @Override
    @NotNull
    public Optional<String> getLanguage(@NotNull R receiver) {
        notNull(receiver, "Receiver must not be null!");
        return Optional.ofNullable(this.data.get(receiver));
    }

    /**
     * Set the language for a receiver.
     *
     * @param lang The language. (the language file's name, EXCLUDING EXTENSION!)
     */
    @Override
    public void setLanguage(@NotNull R receiver, @NotNull String lang) {
        notNull(receiver, "Receiver must not be null!");
        notNull(lang, "Lang must not be null!");
        this.data.put(receiver, lang);
    }

    /**
     * Remove a receivers language entry.
     */
    @Override
    public void removeLanguage(@NotNull R recevier) {
        notNull(recevier, "Receiver must not be null!");
        this.data.remove(recevier);
    }

    /**
     * Retrieve the language for the console.
     *
     * @return The consoles' language. Default: Default Language
     */
    @Override
    @NotNull
    public String getConsoleLanguage() {
        return consoleLanguage;
    }

    /**
     * Set the language for the console.
     *
     * @param consoleLanguage The consoles' language.
     */
    @Override
    public void setConsoleLanguage(@NotNull String consoleLanguage) {
        notNull(consoleLanguage, "Language must not be null!");
        this.consoleLanguage = consoleLanguage;
    }

    /**
     * Not-null validator.
     *
     * @param object The object to validate.
     * @param message The message sent if validation fails.
     * @param <N> Type
     */
    protected static <N> void notNull(N object, String message) {
        //noinspection ConstantConditions
        if (object == null) throw new NullPointerException(message);
    }

}