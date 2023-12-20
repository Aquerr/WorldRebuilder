package io.github.aquerr.worldrebuilder.config;

import io.github.aquerr.worldrebuilder.util.resource.Resource;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationImpl implements Configuration
{
    public static final String CONFIG_FILE_NAME = "config.conf";

    private final Path configDirectoryPath;
    private final Path configPath;
    private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    //Configs
    private Map<Class<? extends AbstractConfig>, AbstractConfig> configs = new HashMap<>();

    public ConfigurationImpl(Path configDir, Resource configAsset) throws IOException
    {
        this.configDirectoryPath = configDir;
        Files.createDirectories(this.configDirectoryPath);

        this.configPath = this.configDirectoryPath.resolve(CONFIG_FILE_NAME);

        try
        {
            if (Files.notExists(this.configPath))
                Files.copy(configAsset.getInputStream(), this.configPath);
        }
        catch (final IOException e)
        {
            throw new IllegalStateException(e);
        }

        this.configLoader = (HoconConfigurationLoader.builder()).path(this.configPath).build();
        loadConfiguration();

        this.configs.put(LangConfig.class, new LangConfig(this.configNode));
        reloadConfiguration();
    }

    public void reloadConfiguration() throws IOException
    {
        loadConfiguration();
        for (AbstractConfig value : this.configs.values())
        {
            value.reload(this.configNode);
        }
    }

    private void loadConfiguration() throws IOException
    {
        configNode = configLoader.load(ConfigurationOptions.defaults().shouldCopyDefaults(true));
    }

    @Override
    public LangConfig getLangConfig()
    {
        return getConfig(LangConfig.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractConfig> T getConfig(Class<T> clazz)
    {
        return (T)this.configs.get(clazz);
    }
}
