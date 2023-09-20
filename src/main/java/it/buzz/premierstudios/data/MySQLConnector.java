package it.buzz.premierstudios.data;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.buzz.premierstudios.Region;
import it.buzz.premierstudios.holder.AbstractPluginHolder;
import it.buzz.premierstudios.holder.Startable;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MySQLConnector extends AbstractPluginHolder implements Startable {

    public final static String ACTIVE_TABLE = "regions";
    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("regions-data-thread").build());
    private HikariDataSource dataSource;

    public MySQLConnector(Region plugin) {
        super(plugin);
    }

    @Override
    public void start() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/minecraft");
        config.setUsername("minecraft");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dataSource = new HikariDataSource(config);

        //Create table
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + ACTIVE_TABLE + " " +
                "(id INT AUTO_INCREMENT  PRIMARY KEY, " +
                "name VARCHAR(36), " +
                "owner VARCHAR(16), " +
                "point_a VARCHAR(32), " +
                "point_b VARCHAR(32)," +
                "members VARCHAR(255))")) {
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop() {
        executor.shutdown();

        plugin.getLogger().info("Waiting for regions-data-thread to end all tasks...");
        try {
            if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                plugin.getLogger().info("All tasks completed for regions-data-thread");
            } else {
                plugin.getLogger().info("Timed out for regions-data-thread");
            }
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().info("An error occured while completing tasks for regions-data-thread");
        }

        dataSource.close();
    }

    public void execute(Consumer<MySQLConnector> consumer) {
        if (Thread.currentThread().getName().equals("regions-data-thread")) consumer.accept(this);
        else executor.execute(() -> {
            try {
                consumer.accept(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Connection connection() throws SQLException {
        return dataSource.getConnection();
    }


}