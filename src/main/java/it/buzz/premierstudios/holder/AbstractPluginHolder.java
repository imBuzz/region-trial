package it.buzz.premierstudios.holder;

import it.buzz.premierstudios.Region;
import org.bukkit.event.Listener;

public abstract class AbstractPluginHolder implements Listener {

    protected final Region plugin;

    protected AbstractPluginHolder(Region plugin) {
        this.plugin = plugin;
    }

    public Region getPlugin() {
        return plugin;
    }
}
