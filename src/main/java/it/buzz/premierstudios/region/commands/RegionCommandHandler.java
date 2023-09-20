package it.buzz.premierstudios.region.commands;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.buzz.premierstudios.Region;
import it.buzz.premierstudios.gui.RegionMonitorGUI;
import it.buzz.premierstudios.holder.AbstractPluginHolder;
import it.buzz.premierstudios.holder.Startable;
import it.buzz.premierstudios.region.region.ImaginaryRegion;
import it.buzz.premierstudios.region.region.metadata.RegionMetadata;
import it.buzz.premierstudios.region.region.wand.WandClipboard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiConsumer;

public class RegionCommandHandler extends AbstractPluginHolder implements Startable {

    private final Cache<UUID, WandClipboard> currentClipboards = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    public RegionCommandHandler(Region plugin) {
        super(plugin);
    }

    @Override
    public void start() {
        registerCommand("region", (sender, args) -> {
            // /duel
            if (args.length < 1){
                sender.sendMessage(ChatColor.AQUA + "Region made by Buzz");

                if (sender.hasPermission("region.monitor")){
                    RegionMonitorGUI.getInventory(plugin.getRegionHandler()).open((Player) sender);
                }
                return;
            }

            switch (args[0].toLowerCase()){
                case "add" -> {
                    if (!sender.hasPermission("region.add")){
                        sender.sendMessage(ChatColor.RED + "You don't have the permission to do that!");
                        return;
                    }

                    if (args.length < 3){
                        sender.sendMessage(ChatColor.RED + "Usage: /region add <name> <player>");
                        return;
                    }

                    ImaginaryRegion region = plugin.getRegionHandler().getRegion(args[1]);
                    if (region == null){
                        sender.sendMessage(ChatColor.RED + "No region found with this name");
                        return;
                    }

                    Player target = Bukkit.getPlayerExact(args[2]);
                    if (target == null){
                        sender.sendMessage(ChatColor.RED + "This player is not online!");
                        return;
                    }

                    if (target == sender){
                        sender.sendMessage(ChatColor.RED + "You cannot invite yourself!");
                        return;
                    }

                    if (!region.isOwner((Player) sender)){
                        sender.sendMessage(ChatColor.RED + "You have to be the owner of the region in order to invite other players!");
                        return;
                    }

                    region.allow(target.getName());

                    sender.sendMessage(ChatColor.GREEN + "You have invited " + target.getName());
                    target.sendMessage(ChatColor.GREEN + "You have been invited by: " + sender.getName() + " into the " + region.getName() + " region");
                }
                case "create" -> {
                    if (!sender.hasPermission("region.create")){
                        sender.sendMessage(ChatColor.RED + "You don't have the permission to do that!");
                        return;
                    }

                    if (args.length < 2){
                        sender.sendMessage(ChatColor.RED + "Usage: /region create <name>");
                        return;
                    }

                    Player player = ((Player) sender);

                    if (currentClipboards.asMap().containsKey(player.getUniqueId())){
                        WandClipboard clipboard = currentClipboards.asMap().get(player.getUniqueId());

                        TreeSet<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                        set.add(sender.getName());

                        RegionMetadata metadata = new RegionMetadata(args[1],
                                sender.getName(), clipboard.getPointA(), clipboard.getPointB(), set);

                        currentClipboards.invalidate(player.getUniqueId());
                        plugin.getRegionHandler().makeNewRegion(metadata);
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + "You have to make a region first, use /region wand");
                    }

                }
                case "redefine" -> {
                    if (!sender.hasPermission("region.redefine")){
                        sender.sendMessage(ChatColor.RED + "You don't have the permission to do that!");
                        return;
                    }

                    if (args.length < 2){
                        sender.sendMessage(ChatColor.RED + "Usage: /region redefine <name>");
                        return;
                    }

                    Player player = ((Player) sender);

                    if (currentClipboards.asMap().containsKey(player.getUniqueId())){
                        WandClipboard clipboard = currentClipboards.asMap().get(player.getUniqueId());

                        ImaginaryRegion region = plugin.getRegionHandler().getRegion(args[1]);
                        if (region == null){
                            sender.sendMessage(ChatColor.RED + "No region found with that name");
                            return;
                        }


                        region.setPointA(clipboard.getPointA());
                        region.setPointB(clipboard.getPointB());

                        plugin.getRegionHandler().refresh(region);

                        sender.sendMessage(ChatColor.GREEN + "Region redefined!");
                    }
                    else {
                        sender.sendMessage(ChatColor.RED + "You have to make a region first, use /region wand");
                    }

                }
                case "wand" -> {
                    if (!sender.hasPermission("region.wand")){
                        sender.sendMessage(ChatColor.RED + "You don't have the permission to do that!");
                        return;
                    }

                    ((Player) sender).getInventory().addItem(new ItemStack(Material.DIAMOND_AXE));
                    sender.sendMessage(ChatColor.GREEN + "You got a Wand!");
                }
                case "remove" -> {
                    if (!sender.hasPermission("region.remove")){
                        sender.sendMessage(ChatColor.RED + "You don't have the permission to do that!");
                        return;
                    }

                    if (args.length < 3){
                        sender.sendMessage(ChatColor.RED + "Usage: /region remove <name> <player>");
                        return;
                    }

                    ImaginaryRegion region = plugin.getRegionHandler().getRegion(args[1]);
                    if (region == null){
                        sender.sendMessage(ChatColor.RED + "No region found with this name");
                        return;
                    }

                    Player target = Bukkit.getPlayerExact(args[2]);
                    if (target == null){
                        sender.sendMessage(ChatColor.RED + "This player is not online!");
                        return;
                    }

                    if (target == sender){
                        sender.sendMessage(ChatColor.RED + "You cannot remove yourself!");
                        return;
                    }

                    if (!region.isOwner((Player) sender)){
                        sender.sendMessage(ChatColor.RED + "You have to be the owner of the region in order to remove other players!");
                        return;
                    }

                    region.disallow(target.getName());

                    sender.sendMessage(ChatColor.GREEN + "You have invited " + target.getName());
                    target.sendMessage(ChatColor.COLOR_CHAR + "You have been remove by: " + sender.getName() + " from the " + region.getName() + " region");
                }
                case "whitelist" -> {
                    if (!sender.hasPermission("region.whitelist")){
                        sender.sendMessage(ChatColor.RED + "You don't have the permission to do that!");
                        return;
                    }

                    if (args.length < 2){
                        sender.sendMessage(ChatColor.RED + "Usage: /region whitelist <name>");
                        return;
                    }

                    ImaginaryRegion region = plugin.getRegionHandler().getRegion(args[1]);
                    if (region == null){
                        sender.sendMessage(ChatColor.RED + "No region found with this name");
                        return;
                    }

                    sender.sendMessage(" ");
                    sender.sendMessage(ChatColor.AQUA + " Region Players");
                    region.users().forEach(user -> sender.sendMessage(ChatColor.AQUA + " " + user));
                    sender.sendMessage(" ");

                }
                default -> {
                    sender.sendMessage(ChatColor.RED + "Usage: /region add/remove/whitelist/wand/create/redefine");
                }
            }
        });

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void stop() {

    }

    @EventHandler(priority = EventPriority.LOW)
    private void wandUsage(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.DIAMOND_AXE){
            WandClipboard wandClipboard = currentClipboards.get(player.getUniqueId(), key -> new WandClipboard());

            if (event.getClickedBlock() != null && event.getClickedBlock().getType() != Material.AIR){
                if (event.getAction().toString().contains("LEFT_")){
                    event.setCancelled(true);
                    wandClipboard.setPointA(event.getClickedBlock().getLocation());

                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Point A created");
                }
                else if (event.getAction().toString().contains("RIGHT_")){
                    event.setCancelled(true);
                    wandClipboard.setPointB(event.getClickedBlock().getLocation());

                    player.sendMessage(ChatColor.LIGHT_PURPLE + "Point B created");
                }
            }
        }
    }

    private void registerCommand(String command, BiConsumer<CommandSender, String[]> executor) {
        plugin.getCommand(command).setExecutor((sender, command1, label, args) -> {
            executor.accept(sender, args);
            return true;
        });
    }


}
