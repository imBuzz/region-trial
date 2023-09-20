package it.buzz.premierstudios.region.region.metadata;

import it.buzz.premierstudios.region.region.ImaginaryRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Set;
import java.util.TreeSet;

public record RegionMetadata(String name, String owner, Location pointA, Location pointB, TreeSet<String> users) {

    public ImaginaryRegion toRegion(int id){
        return new ImaginaryRegion(id, name, owner, pointA, pointB, users);
    }

    public String usersString(){
        StringBuilder builder = new StringBuilder();
        users.forEach(user -> builder.append(users.size() > 1 ? user + ":" : user));
        return builder.toString();
    }

    public String pointAString(){
        return serializeLocation(pointA);
    }

    public String pointBString(){
        return serializeLocation(pointB);
    }

    public static Location deserializeLocation(String string) {
        if (string == null) return null;
        String[] split = string.split(":");
        return new Location(Bukkit.getWorld(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
    }

    public static String serializeLocation(Location location) {
        if (location == null) return null;
        return location.getWorld().getName() + ":" + location.getX() + ":" + location.getY() + ":" + location.getZ() + ":" + location.getYaw() + ":" + location.getPitch();
    }

}
