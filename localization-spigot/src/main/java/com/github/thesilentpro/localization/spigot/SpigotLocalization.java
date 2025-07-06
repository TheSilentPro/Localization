package com.github.thesilentpro.localization.spigot;

import com.github.thesilentpro.localization.api.Localization;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.github.thesilentpro.localization.api.AbstractLocalization;
import com.github.thesilentpro.localization.api.ConsoleLogLevel;

import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of {@link Localization} for the spigot platform.
 *
 * @author TheSilentPro (Silent)
 */
public class SpigotLocalization extends AbstractLocalization<String, String, UUID> {

    @SuppressWarnings("RegExpRedundantEscape")
    private final Pattern ARGS_PATTERN = Pattern.compile("\\$\\{(\\d+)\\}\n", Pattern.CASE_INSENSITIVE); // (\{\$arg(\d+)\}) | Example: ${0}

    /**
     * If true, messages will be colorized with '&' color codes.
     *
     * @see #setColorize(boolean)
     */
    private boolean colorize = true;

    /**
     * Creates a new {@link Localization} instance.
     *
     * @param defaultLanguage The default language. Default: en
     */
    public SpigotLocalization(@NotNull JavaPlugin plugin, @Nullable String defaultLanguage) {
        super(defaultLanguage);
        setConsoleLogFunction((level, message) -> {
            Logger logger = plugin.getLogger();
            Level lvl = toJULLevel(level);
            if (logger.isLoggable(lvl)) {
                logger.log(lvl, message);
            }
        });
    }

    public SpigotLocalization(@NotNull JavaPlugin plugin) {
        this(plugin, null);
    }

    @Override
    @NotNull
    public Optional<String> getMessage(@NotNull UUID uuid, @NotNull String key) {
        Optional<String> message = super.getMessage(uuid, key);
        if (message.isEmpty()) {
            return Optional.empty();
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            message = Optional.of(PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), message.get()));
        }

        return message;
    }


    @Override
    public void sendTranslatedMessage(@NotNull UUID receiver, @NotNull String message) {
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
    public void sendMessage(@NotNull UUID receiver, @NotNull String key, @Nullable UnaryOperator<String> function, @Nullable String... args) {
        notNull(receiver, "Receiver can not be null!");
        notNull(key, "Key can not be null!");

        getMessage(receiver, key).ifPresent(message -> {
            if (args != null) {
                for (String arg : args) {
                    if (arg != null) {
                        Matcher matcher = ARGS_PATTERN.matcher(message);
                        while (matcher.find()) {
                            message = matcher.replaceAll(args[Integer.parseInt(matcher.group(2))]);
                        }
                    }
                }
            }

            // Apply function
            message = function != null ? function.apply(message) : message;
            sendTranslatedMessage(receiver, colorize ? ChatColor.translateAlternateColorCodes('&', message) : message);
        });
    }

    @Override
    public void sendConsoleMessage(ConsoleLogLevel level, @NotNull String key, @Nullable UnaryOperator<String> function, String @Nullable [] args) {
        notNull(key, "Key must not be null!");

        getConsoleMessage(key).ifPresent(message -> {
            if (args != null) {
                for (String arg : args) {
                    if (arg != null) {
                        Matcher matcher = ARGS_PATTERN.matcher(message);
                        while (matcher.find()) {
                            message = matcher.replaceAll(args[Integer.parseInt(matcher.group(2))]);
                        }
                    }
                }
            }

            // Apply function
            message = function != null ? function.apply(message) : message;
            sendTranslatedConsoleMessage(level, colorize ? ChatColor.translateAlternateColorCodes('&', message) : message);
        });
    }

    // Auto Resolve

    public void sendMessage(CommandSender receiver, String key, @Nullable UnaryOperator<String> function, @Nullable String... args) {
        if (receiver instanceof ConsoleCommandSender || receiver instanceof RemoteConsoleCommandSender) {
            sendConsoleMessage(key, function, args);
        } else if (receiver instanceof Player player) {
            sendMessage(player.getUniqueId(), key, function, args);
        }
    }

    public void sendMessage(CommandSender receiver, String key, @Nullable UnaryOperator<String> function) {
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

    public void setColorize(boolean colorize) {
        this.colorize = colorize;
    }

    public boolean isColorize() {
        return colorize;
    }

    private static Level toJULLevel(ConsoleLogLevel level) {
        switch (level) {
            case WARN -> {
                return Level.WARNING;
            }
            case ERROR -> {
                return Level.SEVERE;
            }
            case DEBUG -> {
                return Level.FINE;
            }
            case TRACE -> {
                return Level.FINEST;
            }
            case null, default -> {
                return Level.INFO;
            }
        }
    }

}
