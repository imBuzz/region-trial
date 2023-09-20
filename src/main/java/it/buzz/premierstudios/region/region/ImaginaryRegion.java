package it.buzz.premierstudios.region.region;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static it.buzz.premierstudios.region.region.metadata.RegionMetadata.serializeLocation;

@RequiredArgsConstructor @AllArgsConstructor
public class ImaginaryRegion {

    @Getter private final int id;
    @Getter private String name, owner;
    @Setter private Location pointA, pointB;
    private TreeSet<String> allowedUsers;

    public boolean isInRegion(Location location){
        double minX = Math.min(pointA.getX(), pointB.getX()), minY = Math.min(pointA.getY(), pointB.getY()), minZ = Math.min(pointA.getZ(), pointB.getZ());
        double maxX = Math.max(pointA.getX(), pointB.getX()), maxY = Math.max(pointA.getY(), pointB.getY()), maxZ = Math.max(pointA.getZ(), pointB.getZ()); // Maximum coordinates

        return location.getX() >= minX && location.getX()
                <= maxX && location.getY() >= minY && location.getY() <=
                maxY && location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public void allow(String name){
        allowedUsers.add(name);
    }
    public boolean isAllowed(String name){
        return allowedUsers.contains(name);
    }
    public void disallow(String name){
        allowedUsers.remove(name);
    }

    public boolean isOwner(Player player){
        return owner.equalsIgnoreCase(player.getName());
    }


    public Set<Chunk> chunks() {
        Set<Chunk> chunks = new HashSet<>();
        for (int x = (int) Math.min(pointA.getX(), pointB.getX()); x <  Math.max(pointA.getX(), pointB.getX()); x++) {
            for (int z = (int) Math.min(pointA.getZ(), pointB.getZ()); z < Math.max(pointA.getZ(), pointB.getZ()); z++) {
                try {
                    Chunk chunk = pointA.getWorld().getChunkAt(x >> 4, z >> 4);
                    if (chunk != null) chunks.add(chunk);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return chunks;
    }

    public Set<String> users() {
        return allowedUsers;
    }

    public String usersString(){
        StringBuilder builder = new StringBuilder();
        allowedUsers.forEach(user -> builder.append(allowedUsers.size() > 1 ? user + ":" : user));
        return builder.toString();
    }

    public String pointAString(){
        return serializeLocation(pointA);
    }

    public String pointBString(){
        return serializeLocation(pointB);
    }

}
