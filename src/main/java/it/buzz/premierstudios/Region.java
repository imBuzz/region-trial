package it.buzz.premierstudios;

import it.buzz.premierstudios.data.MySQLConnector;
import it.buzz.premierstudios.region.RegionHandler;
import it.buzz.premierstudios.region.commands.RegionCommandHandler;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class Region extends JavaPlugin {

    @Getter private RegionHandler regionHandler;
    @Getter private MySQLConnector connector;
    @Getter private RegionCommandHandler commandHandler;

    @Override
    public void onEnable() {
        connector = new MySQLConnector(this);
        connector.start();

        regionHandler = new RegionHandler(this);
        regionHandler.start();

        commandHandler = new RegionCommandHandler(this);
        commandHandler.start();
    }

    @Override
    public void onDisable() {
        commandHandler.stop();
        regionHandler.stop();
        connector.stop();
    }





}
