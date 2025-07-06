package com.github.thesilentpro.localization.api;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a language and its messages.
 *
 * @author TheSilentPro (Silent)
 */
public class Language<T> {

    private final String id;
    private Map<String, T> messages;

    public Language(String id, Map<String, T> messages) {
        this.id = id;
        this.messages = messages;
    }

    public String getId() {
        return id;
    }

    public void setMessages(Map<String,T> messages) {
        this.messages = messages;
    }

    public void setMessage(String key, T message) {
        this.messages.put(key, message);
    }

    public Optional<T> getMessage(String key) {
        return Optional.ofNullable(this.messages.get(key));
    }

    public Map<String,T> getMessages() {
        return Collections.unmodifiableMap(messages);
    }

}