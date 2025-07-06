package com.github.thesilentpro.localization.paper;

import com.github.thesilentpro.localization.api.Localization;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;
import com.github.thesilentpro.localization.api.AbstractLocalization;
import com.github.thesilentpro.localization.api.ConsoleLogLevel;

import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Implementation of {@link Localization} for the paper platform.
 *
 * @author TheSilentPro (Silent)
 */
public class PaperLocalization extends AbstractLocalization<Component, String, UUID> {

    @SuppressWarnings("RegExpRedundantEscape")
    private Pattern ARGS_PATTERN = Pattern.compile("\\$\\{(?:(\\d+)(\\+)?|(\\*))\\}", Pattern.CASE_INSENSITIVE); // Example: ${0}, ${2+}, ${*}

    /**
     * Creates a new {@link Localization} instance.
     *
     * @param defaultLanguage The default language. Default: en
     */
    public PaperLocalization(@NotNull JavaPlugin plugin, @Nullable String defaultLanguage) {
        super(defaultLanguage);
        setConsoleLogFunction((level, message) -> {
            ComponentLogger logger = plugin.getComponentLogger();
            Level lvl = toSLF4JLevel(level);
            if (logger.isEnabledForLevel(lvl)) {
                switch (lvl) {
                    case INFO -> logger.info(message);
                    case WARN -> logger.warn(message);
                    case ERROR -> logger.error(message);
                    case DEBUG -> logger.debug(message);
                    case TRACE -> logger.trace(message);
                }
            }
        });
    }

    public PaperLocalization(@NotNull JavaPlugin plugin) {
        this(plugin, null);
    }

    @Override
    @NotNull
    public Optional<Component> getMessage(@NotNull UUID uuid, @NotNull String key) {
        Optional<Component> message = super.getMessage(uuid, key);
        if (message.isEmpty()) {
            return message;
        }

        String raw = MiniMessage.miniMessage().serialize(message.get());

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            raw = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), raw);
        }

        return Optional.of(MiniMessage.miniMessage().deserialize(raw));
    }

    @Override
    public void sendTranslatedMessage(@NotNull UUID receiver, @NotNull Component message) {
        notNull(receiver, "UUID must not be null!");
        notNull(message, "Message must not be null!");

        Entity entity = Bukkit.getEntity(receiver);
        if (entity == null) {
            //noinspection UnnecessaryToStringCall
            throw new IllegalArgumentException("Invalid receiver with uuid: " + receiver.toString());
        }

        entity.sendMessage(message);
    }

    public void sendMessage(@NotNull UUID receiver, @NotNull String key, @Nullable UnaryOperator<Component> function, String... args) {
        notNull(receiver, "Receiver must not be null!");
        notNull(key, "Key must not be null!");

        this.getMessage(receiver, key).ifPresent((message) -> {
            if (args != null && args.length > 0) {
                message = message.replaceText(builder -> builder
                        .match(this.ARGS_PATTERN)
                        .replacement((matcher, b) -> {
                            // Check if it's a digit + optional plus
                            String digitGroup = matcher.group(1);
                            String plusGroup = matcher.group(2);
                            String starGroup = matcher.group(3);

                            if (starGroup != null) {
                                // ${*} → all args joined by space
                                return Component.text(String.join(" ", args));
                            }

                            try {
                                int index = Integer.parseInt(digitGroup) - 1; // 1-based to 0-based
                                boolean isPlus = plusGroup != null;

                                if (index < 0 || index >= args.length) {
                                    return Component.text("");
                                }

                                if (isPlus) {
                                    // Join all args from index onward
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = index; i < args.length; i++) {
                                        if (i > index) sb.append(" ");
                                        sb.append(args[i]);
                                    }
                                    return Component.text(sb.toString());
                                } else {
                                    return Component.text(args[index] != null ? args[index] : "");
                                }
                            } catch (NumberFormatException e) {
                                return Component.text("");
                            }
                        })
                );
            } else if (args != null && args.length == 0) {
                // Handle case: args array empty but placeholders present — maybe clear?
                message = message.replaceText(builder -> builder
                        .match(this.ARGS_PATTERN)
                        .replacement(Component.text("")));
            }

            if (function != null) {
                message = function.apply(message);
            }

            this.sendTranslatedMessage(receiver, message);
        });
    }

    @Override
    public @NotNull Optional<Component> getConsoleMessage(@NotNull String key) {
        Optional<Component> message = super.getConsoleMessage(key);
        if (message.isEmpty()) {
            return message;
        }

        String raw = MiniMessage.miniMessage().serialize(message.get());

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            raw = PlaceholderAPI.setPlaceholders(null, raw); // apparently, null is for console
        }

        return Optional.of(MiniMessage.miniMessage().deserialize(raw));
    }

    @Override
    public void sendConsoleMessage(ConsoleLogLevel level, @NotNull String key, @Nullable UnaryOperator<Component> function, String... args) {
        notNull(key, "Key must not be null!");

        getConsoleMessage(key).ifPresent(message -> {
            if (args != null && args.length > 0) {
                message = message.replaceText(builder -> builder
                        .match(ARGS_PATTERN)
                        .replacement((matcher, b) -> {
                            String digitGroup = matcher.group(1);
                            String plusGroup = matcher.group(2);
                            String starGroup = matcher.group(3);

                            if (starGroup != null) {
                                // ${*} → all args joined by space
                                return Component.text(String.join(" ", args));
                            }

                            try {
                                int index = Integer.parseInt(digitGroup) - 1; // Convert 1-based to 0-based index
                                boolean isPlus = plusGroup != null;

                                if (index < 0 || index >= args.length) {
                                    return Component.text("");
                                }

                                if (isPlus) {
                                    // Join all args from index onward
                                    StringBuilder sb = new StringBuilder();
                                    for (int i = index; i < args.length; i++) {
                                        if (i > index) sb.append(" ");
                                        sb.append(args[i]);
                                    }
                                    return Component.text(sb.toString());
                                } else {
                                    return Component.text(args[index] != null ? args[index] : "");
                                }
                            } catch (NumberFormatException e) {
                                return Component.text("");
                            }
                        })
                );
            } else if (args != null && args.length == 0) {
                // If args array is empty but placeholders exist, clear placeholders
                message = message.replaceText(builder -> builder
                        .match(ARGS_PATTERN)
                        .replacement(Component.text("")));
            }

            // Apply optional transformation function
            message = function != null ? function.apply(message) : message;

            sendTranslatedConsoleMessage(level, message);
        });
    }

    // Auto Resolve

    public void sendMessage(CommandSender receiver, String key, @Nullable UnaryOperator<Component> function, @Nullable String... args) {
        if (receiver instanceof ConsoleCommandSender || receiver instanceof RemoteConsoleCommandSender) {
            sendConsoleMessage(key, function, args);
        } else if (receiver instanceof Player player) {
            sendMessage(player.getUniqueId(), key, function, args);
        }
    }

    public void sendMessage(CommandSender receiver, String key, @Nullable String... args) {
        sendMessage(receiver, key, null, args);
    }

    public void sendMessage(CommandSender receiver, String key, @Nullable UnaryOperator<Component> function) {
        sendMessage(receiver, key, function, (String[]) null);
    }

    public void sendMessage(CommandSender receiver, String key) {
        sendMessage(receiver, key, null, (String[]) null);
    }

    public void sendMessage(String key, CommandSender... receivers) {
        for (CommandSender receiver : receivers) {
            sendMessage(receiver, key);
        }
    }

    public void setArgsPattern(Pattern pattern) {
        this.ARGS_PATTERN = pattern;
    }

    public Pattern getArgsPattern() {
        return ARGS_PATTERN;
    }

    @NotNull
    public static Level toSLF4JLevel(@Nullable ConsoleLogLevel level) {
        switch (level) {
            case WARN -> {
                return Level.WARN;
            }
            case ERROR -> {
                return Level.ERROR;
            }
            case DEBUG -> {
                return Level.DEBUG;
            }
            case TRACE -> {
                return Level.TRACE;
            }
            case null, default -> {
                return Level.INFO;
            }
        }
    }

}