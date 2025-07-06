package com.github.thesilentpro.localization.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.github.thesilentpro.localization.api.loader.LanguageLoader;
import com.github.thesilentpro.localization.api.loader.ReceiverDataLoader;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

/**
 * Represents a localization instance.
 *
 * @param <T> Message return type
 * @param <A> Argument type
 * @param <R> Receiver type
 */
@SuppressWarnings("unchecked")
public interface Localization<T, A, R> {

    /**
     * Retrieve a message by the receiver's language and the key.
     *
     * @param receiver The receiver.
     * @param key The message key.
     * @return If present, the message, otherwise an empty {@link Optional}.
     */
    @NotNull
    Optional<T> getMessage(@NotNull R receiver, @NotNull String key);

    /**
     * Sends a translated message to a receiver.
     *
     * @param receiver The receiver.
     * @param message The message to be sent.
     */
    void sendTranslatedMessage(@NotNull R receiver, @NotNull T message);

    /**
     * Sends a message to a receiver with an optional message transformation function and arguments.
     *
     * @param receiver The receiver.
     * @param key The message key.
     * @param function Optional transformation function for the message.
     * @param args Optional arguments to be used in the message.
     */
    void sendMessage(@NotNull R receiver, @NotNull String key, @Nullable UnaryOperator<T> function, @Nullable A... args);

    /**
     * Sends a message to a receiver with an optional message transformation function.
     *
     * @param receiver The receiver.
     * @param key The message key.
     * @param function Optional transformation function for the message.
     */
    default void sendMessage(@NotNull R receiver, @NotNull String key, @Nullable UnaryOperator<T> function) {
        sendMessage(receiver, key, function, (A[]) null);
    }

    /**
     * Sends a message to a receiver with optional arguments.
     *
     * @param receiver The receiver.
     * @param key      The message key.
     * @param args     Optional arguments to be used in the message.
     */
    default void sendMessage(@NotNull R receiver, @NotNull String key, @Nullable A... args) {
        sendMessage(receiver, key, null, args);
    }

    /**
     * Sends a message to a receiver.
     *
     * @param receiver The receiver.
     * @param key The message key.
     */
    default void sendMessage(@NotNull R receiver, @NotNull String key) {
        sendMessage(receiver, key, null, (A[]) null);
    }

    /**
     * Sends a message to multiple receivers.
     *
     * @param key The message key.
     * @param receivers Receivers to send the message to.
     */
    void sendMessages(@NotNull String key, @NotNull R... receivers);

    // Console

    /**
     * Sends a translated message to the console.
     *
     * @param message The message to be sent to the console.
     */
    void sendTranslatedConsoleMessage(ConsoleLogLevel level, T message);

    /**
     * Sends a message to the console with an optional message transformation function and arguments.
     *
     * @param key      The message key.
     * @param function Optional transformation function for the message.
     * @param args     Optional arguments to be used in the message.
     */
    void sendConsoleMessage(@Nullable ConsoleLogLevel level, @NotNull String key, @Nullable UnaryOperator<T> function, @Nullable A... args);

    default void sendConsoleMessage(@NotNull String key, @Nullable UnaryOperator<T> function, @Nullable A... args) {
        sendConsoleMessage(ConsoleLogLevel.INFO, key, function, args);
    }

    /**
     * Sends a message to the console with an optional message transformation function.
     *
     * @param key The message key.
     * @param function Optional transformation function for the message.
     */
    default void sendConsoleMessage(@Nullable ConsoleLogLevel level, @NotNull String key, @Nullable UnaryOperator<T> function) {
        sendConsoleMessage(level, key, function, (A[]) null);
    }

    /**
     * Sends a message to the console with optional arguments.
     *
     * @param key  The message key.
     * @param args Optional arguments to be used in the message.
     */
    default void sendConsoleMessage(@Nullable ConsoleLogLevel level, @NotNull String key, @Nullable A... args) {
        sendConsoleMessage(level, key, null, args);
    }

    /**
     * Sends a message to the console.
     *
     * @param key The message key.
     */
    default void sendConsoleMessage(@Nullable ConsoleLogLevel level, @NotNull String key) {
        sendConsoleMessage(level, key, null, (A[]) null);
    }

    /**
     * Sends a message to the console.
     *
     * @param key The message key.
     */
    default void sendConsoleMessage(@NotNull String key) {
        sendConsoleMessage(ConsoleLogLevel.INFO, key);
    }

    /**
     * Retrieves a message for the console.
     *
     * @param key The message key.
     * @return If present, the message for the console, otherwise an empty {@link Optional}.
     */
    @NotNull
    Optional<T> getConsoleMessage(@NotNull String key);

    /**
     * Sets the function to log messages to the console.
     *
     * @param consoleLogFunction The function to log messages to the console.
     */
    void setConsoleLogFunction(@NotNull BiConsumer<ConsoleLogLevel, T> consoleLogFunction);

    // Loaders

    /**
     * Load all languages.
     *
     * @param loader The loader responsible for loading language data.
     * @return Number of files loaded.
     */
    int loadLanguages(@NotNull LanguageLoader<T> loader) throws IOException;

    /**
     * Load the receiver languages.
     *
     * @param loader The loader responsible for loading receiver data.
     * @return Number of receivers loaded.
     */
    int loadReceiverData(@NotNull ReceiverDataLoader<R> loader);

    /**
     * Save the receiver languages.
     *
     * @param loader The loader responsible for saving receiver data.
     */
    void saveReceiverData(@NotNull ReceiverDataLoader<R> loader);

    /**
     * Retrieve a {@link Map} containing all language/message data.
     *
     * @return The language/message data. Format: Language, Messages.
     */
    @NotNull
    Map<String, Language<T>> getLanguages();

    /**
     * Retrieve the default language.
     *
     * @return The default language.
     */
    @NotNull
    String getDefaultLanguage();

    /**
     * Retrieve an unmodifiable {@link Map} of all loaded language settings for each receiver.
     *
     * @return The language map. Format: Receiver, Language.
     */
    @NotNull
    Map<R, String> getReceiverData();

    /**
     * Retrieve the language for a specific receiver.
     *
     * @param receiver The receiver.
     * @return Optional language.
     */
    @NotNull
    Optional<String> getLanguage(@NotNull R receiver);

    /**
     * Set the language for a specific receiver.
     *
     * @param receiver The receiver.
     * @param lang The language.
     */
    void setLanguage(@NotNull R receiver, @NotNull String lang);

    /**
     * Remove a receiver's language entry.
     *
     * @param receiver The receiver.
     */
    void removeLanguage(@NotNull R receiver);

    /**
     * Retrieve the language for the console.
     *
     * @return The console's language.
     */
    @NotNull
    String getConsoleLanguage();

    /**
     * Set the language for the console.
     *
     * @param consoleLanguage The console's language.
     */
    void setConsoleLanguage(@NotNull String consoleLanguage);

}