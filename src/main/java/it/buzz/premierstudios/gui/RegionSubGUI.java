package it.buzz.premierstudios.gui;

import fr.minuskube.inv.ClickableItem;
import fr.minuskube.inv.SmartInventory;
import fr.minuskube.inv.content.InventoryContents;
import fr.minuskube.inv.content.InventoryProvider;
import fr.minuskube.inv.content.Pagination;
import fr.minuskube.inv.content.SlotIterator;
import it.buzz.premierstudios.data.utils.Pair;
import it.buzz.premierstudios.gui.utils.ItemBuilder;
import it.buzz.premierstudios.region.RegionHandler;
import it.buzz.premierstudios.region.conversation.PlayerResponse;
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

    private final RegionHandler regionHandler;
    private final ImaginaryRegion region;

    public static SmartInventory getInventory(RegionHandler handler, ImaginaryRegion region) {
        INVENTORY = SmartInventory.builder()
                .id("regions_sub")
                .provider(new RegionSubGUI(handler, region))
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
                    regionHandler.getPlayersReponses().put(event.getWhoClicked().getName(),
                            new Pair<>(PlayerResponse.REGION_RENAME, region.getName()));

                    event.getWhoClicked().sendMessage("");
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Send in chat the new name");
                    event.getWhoClicked().sendMessage("");
                }
        ));

        contents.set(1, 3, ClickableItem.of(
                new ItemBuilder(Material.CHEST)
                        .setName(ChatColor.GRAY + "Whitelist ADD")
                        .get(),
                event -> {
                    event.getWhoClicked().closeInventory();
                    regionHandler.getPlayersReponses().put(event.getWhoClicked().getName(),
                            new Pair<>(PlayerResponse.WHITELIST_ADD, region.getName()));

                    event.getWhoClicked().sendMessage("");
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Send in chat the player name");
                    event.getWhoClicked().sendMessage("");
                }
        ));

        contents.set(1, 5, ClickableItem.of(
                new ItemBuilder(Material.CHEST)
                        .setName(ChatColor.GRAY + "Whitelist REMOVE")
                        .get(),
                event -> {
                    event.getWhoClicked().closeInventory();
                    regionHandler.getPlayersReponses().put(event.getWhoClicked().getName(),
                            new Pair<>(PlayerResponse.WHITELIST_REMOVE, region.getName()));

                    event.getWhoClicked().sendMessage("");
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Send in chat the player name");
                    event.getWhoClicked().sendMessage("");
                }
        ));

        contents.set(1, 7, ClickableItem.of(
                new ItemBuilder(Material.CHEST)
                        .setName(ChatColor.GRAY + "Redefine")
                        .get(),
                event -> {
                    event.getWhoClicked().closeInventory();

                    event.getWhoClicked().sendMessage("");
                    event.getWhoClicked().sendMessage(ChatColor.GREEN + "Do /region redefine <name>");
                    event.getWhoClicked().sendMessage("");
                }
        ));

    }

    @Override
    public void update(Player player, InventoryContents contents) {

    }




}
