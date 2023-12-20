package io.github.aquerr.worldrebuilder.config;

import org.spongepowered.configurate.CommentedConfigurationNode;

public class LangConfig extends AbstractConfig
{
    private String languageTag = "en";

    public LangConfig(CommentedConfigurationNode configNode)
    {
        super(configNode);
    }

    @Override
    public void reload()
    {
        this.languageTag = getString("en", "language");
    }

    public String getLanguageTag()
    {
        return languageTag;
    }
}
