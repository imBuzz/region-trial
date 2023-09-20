package it.buzz.premierstudios.region;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.buzz.premierstudios.Region;
import it.buzz.premierstudios.data.MySQLConnector;
import it.buzz.premierstudios.holder.AbstractPluginHolder;
import it.buzz.premierstudios.holder.Startable;
import it.buzz.premierstudios.region.region.ImaginaryRegion;
import it.buzz.premierstudios.region.region.metadata.RegionMetadata;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RegionHandler extends AbstractPluginHolder implements Startable {

    private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("region-thread").build());
    private final ConcurrentHashMap<String, Set<ImaginaryRegion>> regions = new ConcurrentHashMap<>();

    public RegionHandler(Region plugin) {
        super(plugin);
    }

    public void makeNewRegion(RegionMetadata metadata){
        executor.execute(() -> {
            try (Connection connection = plugin.getConnector().connection(); PreparedStatement statement =
                    connection.prepareStatement("INSERT INTO " + MySQLConnector.ACTIVE_TABLE +
                            " (name, owner, point_a, point_b, members) values (?, ?, ?, ?, ?);",
                            Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, metadata.name());
                statement.setString(2, metadata.owner());
                statement.setString(3, metadata.pointAString());
                statement.setString(4, metadata.pointBString());
                statement.setString(5, metadata.usersString());

                statement.executeUpdate();
                ResultSet set = statement.getGeneratedKeys();
                if (set.next()){
                    int lastGeneratedID = set.getInt(1);

                    ImaginaryRegion region = metadata.toRegion(lastGeneratedID);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        for (Chunk chunk : region.chunks()) {
                            String KEY = chunk.getX() + ":" + chunk.getZ();

                            Set<ImaginaryRegion> sets = regions.getOrDefault(KEY, new HashSet<>());
                            sets.add(region);

                            regions.put(KEY, sets);
                        }

                        Player owner = Bukkit.getPlayerExact(region.getOwner());
                        if (owner != null){
                            owner.sendMessage(ChatColor.GREEN + "Region created with id: " + lastGeneratedID);
                        }
                    }, 1L);
                }
                else {
                    Player owner = Bukkit.getPlayerExact(metadata.owner());
                    if (owner != null){
                        owner.sendMessage(ChatColor.GREEN + "Error while creating a region");
                        return;
                    }
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void start() {
        plugin.getConnector().execute(connector -> {
            try (Connection connection = connector.connection(); PreparedStatement statement =
                    connection.prepareStatement("SELECT * FROM " + MySQLConnector.ACTIVE_TABLE)) {
                try (ResultSet set = statement.executeQuery()) {
                    if (set.next()) {
                        int id = set.getInt(1);

                        String name = set.getString(2);
                        String owner = set.getString(3);
                        String pointA = set.getString(4);
                        String pointB = set.getString(5);
                        String users = set.getString(6);

                        TreeSet<String> setUsers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                        setUsers.addAll(Arrays.asList(users.split(":")));

                        ImaginaryRegion region = new ImaginaryRegion(id, name, owner,
                                RegionMetadata.deserializeLocation(pointA), RegionMetadata.deserializeLocation(pointB), setUsers);


                        for (Chunk chunk : region.chunks()) {
                            String KEY = chunk.getX() + ":" + chunk.getZ();

                            Set<ImaginaryRegion> sets = regions.getOrDefault(KEY, new HashSet<>());
                            sets.add(region);

                            regions.put(KEY, sets);
                        }

                        plugin.getLogger().info("Loaded region with ID " + region.getId());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop() {
        executor.shutdown();

        plugin.getLogger().info("Waiting for region-thread to end all tasks...");
        try {
            if (executor.awaitTermination(5, TimeUnit.SECONDS)) {
                plugin.getLogger().info("All tasks completed for region-thread");
            } else {
                plugin.getLogger().info("Timed out for region-thread");
            }
        } catch (Exception e) {
            e.printStackTrace();
            plugin.getLogger().info("An error occured while completing tasks for region-thread");
        }


        plugin.getConnector().execute(connector -> {
            Set<Integer> idsSaved = new HashSet<>();
            regions.values().forEach(set -> {
                set.forEach(region -> {
                    if (!idsSaved.contains(region.getId())) {
                        idsSaved.add(region.getId());

                        try (Connection connection = connector.connection(); PreparedStatement statement =
                                connection.prepareStatement("INSERT INTO " + MySQLConnector.ACTIVE_TABLE +
                                        " (name, owner, point_a, point_b, members) VALUES (?, ?, ?, ?, ?) " +
                                        "ON DUPLICATE KEY UPDATE " +
                                        "name = '" + region.getName() + "', " +
                                        "owner = '" + region.getOwner() + "', " +
                                        "point_a='" + region.pointAString() + "', " +
                                        "point_b='" + region.pointBString() + "', " +
                                        "members='" + region.usersString() + "' ;")) {

                            statement.setString(1, region.getName());
                            statement.setString(2, region.getOwner());
                            statement.setString(3, region.pointAString());
                            statement.setString(4, region.pointBString());
                            statement.setString(5, region.usersString());

                            statement.execute();
                        }
                        catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                });
            });
        });
    }

    @EventHandler
    private void blockBreak(BlockBreakEvent event){
        getApplicableRegions(event.getBlock().getLocation()).forEach(region -> {
            if (!region.isAllowed(event.getPlayer().getName())){
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    private void blockPlace(BlockPlaceEvent event){
        getApplicableRegions(event.getBlock().getLocation()).forEach(region -> {
            if (!region.isAllowed(event.getPlayer().getName())){
                event.setCancelled(true);
            }
        });
    }

    @EventHandler
    private void interact(PlayerInteractEvent event){
        if (event.getClickedBlock() == null) return;

        getApplicableRegions(event.getClickedBlock().getLocation()).forEach(region -> {
            event.getPlayer().sendMessage(ChatColor.GREEN + "You've interacted with region " + region.getId());

            if (!region.isAllowed(event.getPlayer().getName())){
                event.setCancelled(true);
            }
        });
    }

    public Set<ImaginaryRegion> getApplicableRegions(Location location){
        String KEY = location.getChunk().getX() + ":" + location.getChunk().getZ();
        if (!regions.containsKey(KEY)) return new HashSet<>();

        return Set.copyOf(regions.get(KEY).stream().filter(region -> region.isInRegion(location))
                .collect(Collectors.toSet()));
    }
    public Set<ImaginaryRegion> getRegions(){
        final Set<ImaginaryRegion> allRegions = new HashSet<>();
        regions.values().forEach(allRegions::addAll);

        return Set.copyOf(allRegions);
    }
    public ImaginaryRegion getRegion(String name){
        for (Set<ImaginaryRegion> value : regions.values()) {
            for (ImaginaryRegion region : value) {
                if (region.getName().equalsIgnoreCase(name)) return region;
            }
        }
        return null;
    }

    public void refresh(ImaginaryRegion region){
        for (Chunk chunk : region.chunks()) {
            String KEY = chunk.getX() + ":" + chunk.getZ();

            Set<ImaginaryRegion> sets = regions.getOrDefault(KEY, new HashSet<>());
            sets.add(region);

            regions.put(KEY, sets);
        }
    }

}
