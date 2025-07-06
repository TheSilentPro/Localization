package com.github.thesilentpro.localization.api;

import java.util.function.BiConsumer;

/**
 * Log levels for printing console messages using {@link Localization#setConsoleLogFunction(BiConsumer) log functions}.
 *
 * @author TheSilentPro (Silent)
 */
public enum ConsoleLogLevel {

    ERROR,
    WARN,
    INFO,
    DEBUG,
    TRACE;

}