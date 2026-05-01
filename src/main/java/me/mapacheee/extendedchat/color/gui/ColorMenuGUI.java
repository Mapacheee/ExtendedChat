package me.mapacheee.extendedchat.color.gui;

import me.mapacheee.extendedchat.config.EcMessages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class ColorMenuGUI implements InventoryHolder {

    private final Inventory inventory;
    private final EcMessages messages;

    public ColorMenuGUI(EcMessages messages) {
        this.messages = messages;
        this.inventory = Bukkit.createInventory(this, 27,
                MiniMessage.miniMessage().deserialize(messages.colorMenuTitle()));
        init();
    }

    private void init() {
        inventory.setItem(11, buildItem(Material.NAME_TAG, messages.colorNameItem(),
                messages.colorNameItemLore().toArray(new String[0])));

        inventory.setItem(15, buildItem(Material.PAPER, messages.colorMessageItem(),
                messages.colorMessageItemLore().toArray(new String[0])));

        ItemStack bg = buildItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, bg);
            }
        }
    }

    private ItemStack buildItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessage.miniMessage().deserialize(name));
            if (loreLines.length > 0) {
                List<net.kyori.adventure.text.Component> lore = new java.util.ArrayList<>();
                for (String line : loreLines) {
                    lore.add(MiniMessage.miniMessage().deserialize(line));
                }
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NonNull Inventory getInventory() {
        return inventory;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}