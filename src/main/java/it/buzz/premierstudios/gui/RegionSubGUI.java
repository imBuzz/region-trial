package it.buzz.premierstudios.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import it.buzz.premierstudios.gui.utils.ItemBuilder;
import it.buzz.premierstudios.region.RegionHandler;
import it.buzz.premierstudios.region.region.ImaginaryRegion;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class RegionSubGUI implements InventoryProvider {

    private static SmartInventory INVENTORY;
    private final ImaginaryRegion region;

    public static SmartInventory getInventory(ImaginaryRegion region) {
        INVENTORY = SmartInventory.builder()
                .id("regions_sub")
                .provider(new RegionSubGUI(region))
                .size(4, 9)
                .title(ChatColor.DARK_GRAY + "Region #" + region.getId())
                .build();
        return INVENTORY;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.set(1, 1, ClickableItem.of(
                new ItemBuilder(Material.CHEST)
                        .setName(ChatColor.GRAY + "Rename")
                        .get(),
                event -> {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Do /region rename <region> <name>");
                }
        ));

        contents.set(1, 3, ClickableItem.of(
                new ItemBuilder(Material.CHEST)
                        .setName(ChatColor.GRAY + "Whitelist ADD")
                        .get(),
                event -> {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Do /region add <name> <player>");
                }
        ));

        contents.set(1, 5, ClickableItem.of(
                new ItemBuilder(Material.CHEST)
                        .setName(ChatColor.GRAY + "Whitelist REMOVE")
                        .get(),
                event -> {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Do /region remove <name> <player>");
                }
        ));

        contents.set(1, 7, ClickableItem.of(
                new ItemBuilder(Material.CHEST)
                        .setName(ChatColor.GRAY + "Redefine")
                        .get(),
                event -> {
                    event.getWhoClicked().closeInventory();
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Do /region redefine <name>");
                }
        ));

    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }




}
