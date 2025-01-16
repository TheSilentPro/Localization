package tsp.localization.paper;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.event.Level;
import tsp.localization.api.AbstractLocalization;
import tsp.localization.api.ConsoleLogLevel;

import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Implementation of {@link tsp.localization.api.Localization} for the paper platform.
 *
 * @author TheSilentPro (Silent)
 */
public class PaperLocalization extends AbstractLocalization<Component, String, UUID> {

    @SuppressWarnings("RegExpRedundantEscape")
    private final Pattern ARGS_PATTERN = Pattern.compile("\\$\\{(\\d+)\\}\n", Pattern.CASE_INSENSITIVE); // (\{\$arg(\d+)\}) | Example: ${0}

    /**
     * Creates a new {@link tsp.localization.api.Localization} instance.
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

        String raw = PlainTextComponentSerializer.plainText().serialize(message.get());

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            raw = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), raw);
        }

        MiniMessage mm;
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            mm = MiniMessage.builder().tags(TagResolver.builder().resolvers(TagResolver.standard(), papiTag(player)).build()).build();
        } else {
            mm = MiniMessage.miniMessage();
        }

        message = Optional.of(mm.deserialize(raw));

        return message;
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

    /**
     * Creates a tag resolver capable of resolving PlaceholderAPI tags for a given player.
     *
     * @param player the player
     * @return the tag resolver
     *
     * @see <a href="https://docs.advntr.dev/faq.html#how-can-i-use-bukkits-placeholderapi-in-minimessage-messages">Adventure FAQ</a>
     */
    public @NotNull TagResolver papiTag(final @NotNull Player player) {
        return TagResolver.resolver("papi", (argumentQueue, context) -> {
            // Get the string placeholder that they want to use.
            final String papiPlaceholder = argumentQueue.popOr("papi tag requires an argument").value();

            // Then get PAPI to parse the placeholder for the given player.
            final String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, '%' + papiPlaceholder + '%');

            // We need to turn this ugly legacy string into a nice component.
            final Component componentPlaceholder = LegacyComponentSerializer.legacySection().deserialize(parsedPlaceholder);

            // Finally, return the tag instance to insert the placeholder!
            return Tag.selfClosingInserting(componentPlaceholder);
        });
    }

    @NotNull
    private static Level toSLF4JLevel(@Nullable ConsoleLogLevel level) {
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