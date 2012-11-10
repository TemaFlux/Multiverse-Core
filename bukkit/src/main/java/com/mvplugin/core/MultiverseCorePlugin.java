package com.mvplugin.core;

import com.dumptruckman.minecraft.pluginbase.plugin.AbstractBukkitPlugin;
import com.mvplugin.core.api.MultiverseCore;

import java.io.IOException;

/**
 * The primary Bukkit plugin implementation of Multiverse-Core.
 */
// TODO: Some magic that automatically hooks up abstract base libs because we can't know what libs the base uses
public class MultiverseCorePlugin extends AbstractBukkitPlugin<CoreConfig> implements MultiverseCore {
    private static final String COMMAND_PREFIX = "mv";

    private BukkitWorldManager worldManager;

    public MultiverseCorePlugin() {
        this.setPermissionName("multiverse.core");
    }

    @Override
    protected void onPluginLoad() {
        worldManager = new BukkitWorldManager(this);
    }

    @Override
    public String getCommandPrefix() {
        return COMMAND_PREFIX;
    }

    @Override
    protected CoreConfig newConfigInstance() throws IOException {
        return new YamlCoreConfig(this);
    }

    @Override
    protected boolean useDatabase() {
        return false;
    }

    @Override
    public BukkitWorldManager getMVWorldManager() {
        return this.worldManager;
    }
}