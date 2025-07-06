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
    private final Pattern ARGS_PATTERN = Pattern.compile("\\$\\{(\\d+)\\}\n", Pattern.CASE_INSENSITIVE); // (\{\$arg(\d+)\}) | Example: ${0}

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

    @Override
    public void sendMessage(@NotNull UUID receiver, @NotNull String key, @Nullable UnaryOperator<Component> function, @Nullable String... args) {
        notNull(receiver, "Receiver must not be null!");
        notNull(key, "Key must not be null!");

        getMessage(receiver, key).ifPresent(message -> {
            if (args != null) {
                message = message.replaceText(builder -> builder.match(ARGS_PATTERN).replacement((matcher, b) -> Component.text(matcher.group(2))));
            }

            // Apply function
            message = function != null ? function.apply(message) : message;
            sendTranslatedMessage(receiver, message);
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
            if (args != null) {
                message = message.replaceText(builder -> builder.match(ARGS_PATTERN).replacement((matcher, b) -> Component.text(matcher.group(2))));
            }

            // Apply function
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

    public void sendMessage(CommandSender receiver, String key, @Nullable UnaryOperator<Component> function) {
        sendMessage(receiver, key, function, (String[]) null);
    }

    public void sendMessage(CommandSender receiver, String key) {
        sendMessage(receiver, key, null);
    }

    public void sendMessage(String key, CommandSender... receivers) {
        for (CommandSender receiver : receivers) {
            sendMessage(receiver, key);
        }
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