package io.github.aquerr.worldrebuilder.messaging;

import io.github.aquerr.worldrebuilder.WorldRebuilder;
import io.github.aquerr.worldrebuilder.messaging.locale.Localization;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.LinearComponents;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.exception.CommandException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.PropertyResourceBundle;

public class WRMessageSource implements MessageSource
{
    private static final String MESSAGES_FILE_NAME = "messages_%s.properties";
    private static final String JAR_MESSAGES_FILE_PATH = "assets/worldrebuilder/lang/" + MESSAGES_FILE_NAME;
    private static final String JAR_MESSAGES_FILE_PATH_DEFAULT = "assets/worldrebuilder/lang/messages.properties";

    private static class InstanceHolder {
        public static MessageSource INSTANCE = null;
    }

    public static MessageSource getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final Localization localization;

    private WRMessageSource(PropertyResourceBundle resourceBundle)
    {
        this.localization = Localization.forResourceBundle(resourceBundle);
    }

    public static void init(Path messagesDir, String lang)
    {
        String jarMessagesFilePath = JAR_MESSAGES_FILE_PATH_DEFAULT;
        if (!lang.equals("en"))
        {
            jarMessagesFilePath = String.format(JAR_MESSAGES_FILE_PATH, lang);
        }

        Path destMessagesFilePath = messagesDir.resolve(String.format(MESSAGES_FILE_NAME, lang));

        try
        {
            Files.createDirectories(messagesDir);
            generateLangFile(jarMessagesFilePath, destMessagesFilePath);
        }
        catch (IOException e)
        {
            try
            {
                jarMessagesFilePath = JAR_MESSAGES_FILE_PATH_DEFAULT;
                destMessagesFilePath = destMessagesFilePath.resolveSibling("messages_en.properties");
                generateLangFile(jarMessagesFilePath, destMessagesFilePath);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
                throw new IllegalStateException("Could not generate language file!");
            }
            e.printStackTrace();
        }

        PropertyResourceBundle propertyResourceBundle;

        try
        {
            propertyResourceBundle = new PropertyResourceBundle(Files.newInputStream(destMessagesFilePath));
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }

        InstanceHolder.INSTANCE = new WRMessageSource(propertyResourceBundle);
    }

    private static void generateLangFile(String jarLangFilePath, Path fileLangFilePath) throws IOException
    {
        URI langFileUri = WorldRebuilder.getPlugin().getPluginContainer().locateResource(URI.create(jarLangFilePath))
                .orElseThrow(() -> new RuntimeException("Could not locate language file!"));
        InputStream langFilePathStream = langFileUri.toURL().openStream();
        Files.copy(langFilePathStream, fileLangFilePath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Component resolveMessageWithPrefix(String messageKey)
    {
        return resolveMessageWithPrefix(messageKey, new Object[0]);
    }

    @Override
    public Component resolveMessageWithPrefix(String messageKey, Object... args)
    {
        return LinearComponents.linear(WorldRebuilder.PLUGIN_PREFIX, resolveComponentWithMessage(messageKey, args));
    }

    @Override
    public CommandException resolveExceptionWithMessage(String messageKey)
    {
        return resolveExceptionWithMessage(messageKey, new Object[0]);
    }

    @Override
    public CommandException resolveExceptionWithMessage(String messageKey, Object... args)
    {
        return new CommandException(LinearComponents.linear(WorldRebuilder.PLUGIN_ERROR, resolveComponentWithMessage(messageKey, args)));
    }

    @Override
    public CommandException resolveExceptionWithMessageAndThrowable(String messageKey, Throwable throwable)
    {
        return new CommandException(LinearComponents.linear(WorldRebuilder.PLUGIN_ERROR, resolveComponentWithMessage(messageKey)), throwable);
    }

    @Override
    public TextComponent resolveComponentWithMessage(String messageKey)
    {
        return resolveComponentWithMessage(messageKey, new Object[0]);
    }

    @Override
    public TextComponent resolveComponentWithMessage(String messageKey, Object... args)
    {
        args = Arrays.stream(args)
                .map(arg -> {
                    if (arg instanceof Component)
                    {
                        return LegacyComponentSerializer.legacyAmpersand().serialize((Component) arg);
                    }
                    return arg;
                }).toArray();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(resolveMessage(messageKey, args));
    }

    @Override
    public String resolveMessage(String messageKey)
    {
        return this.resolveMessage(messageKey, new Object[0]);
    }

    @Override
    public String resolveMessage(String messageKey, Object... args)
    {
        return MessageFormat.format(this.localization.getMessage(messageKey), args);
    }
}
