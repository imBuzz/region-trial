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
public class RegionMonitorGUI implements InventoryProvider {

    private static SmartInventory INVENTORY;
    private final RegionHandler regionHandler;

    public static SmartInventory getInventory(RegionHandler regionHandler) {
        INVENTORY = SmartInventory.builder()
                .id("regions_monitor")
                .provider(new RegionMonitorGUI(regionHandler))
                .size(4, 9)
                .title(ChatColor.DARK_GRAY + "Regions")
                .build();
        return INVENTORY;
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        List<ClickableItem> items = new ArrayList<>();

        for (ImaginaryRegion region : regionHandler.getRegions()) {
            items.add(ClickableItem.of(
                    new ItemBuilder(Material.DIRT)
                            .setName(ChatColor.GREEN + "Region #" + region.getId())
                            .setLore(
                                    ChatColor.GRAY + "Name: " + ChatColor.WHITE + region.getName(),
                                    ChatColor.GRAY + "Owner: " + ChatColor.WHITE + region.getOwner(),
                                    ChatColor.GRAY + "Members: " + ChatColor.WHITE + region.users().size()
                            )
                            .get(), event -> {
                        RegionSubGUI.getInventory(regionHandler, region).open(player);
                    }
            ));
        }


        Pagination pagination = contents.pagination();
        pagination.setItems(items.toArray(new ClickableItem[0]));
        pagination.setItemsPerPage(27);
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 0, 0));

        contents.set(3, 3, ClickableItem.of(new ItemBuilder(Material.ARROW).setName(ChatColor.GRAY + "Indietro").get(),
                e -> INVENTORY.open(player, pagination.previous().getPage())));
        contents.set(3, 5, ClickableItem.of(new ItemBuilder(Material.ARROW).setName(ChatColor.GRAY + "Avanti").get(),
                e -> INVENTORY.open(player, pagination.next().getPage())));
    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }




}
