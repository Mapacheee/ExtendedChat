package me.mapacheee.extendedchat.color.gui;

import me.mapacheee.extendedchat.color.ColorService;
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
import net.kyori.adventure.text.Component;
import java.util.List;

public class ColorPickerGUI implements InventoryHolder {

    private final Inventory inventory;
    private final ColorService.ColorTarget target;
    private final EcMessages messages;

    public ColorPickerGUI(ColorService.ColorTarget target, EcMessages messages) {
        this.target = target;
        this.messages = messages;
        String title = target == ColorService.ColorTarget.NAME
                ? messages.colorPickerNameTitle()
                : messages.colorPickerMessageTitle();
        this.inventory = Bukkit.createInventory(this, 45,
                MiniMessage.miniMessage().deserialize(title));
        init();
    }

    private void init() {
        Material[] materials = {
                Material.RED_WOOL,       
                Material.ORANGE_WOOL,    
                Material.YELLOW_WOOL,    
                Material.LIME_WOOL,      
                Material.GREEN_WOOL,      
                Material.CYAN_WOOL,      
                Material.LIGHT_BLUE_WOOL, 
                Material.BLUE_WOOL,       
                Material.PURPLE_WOOL,    
                Material.MAGENTA_WOOL,   
                Material.WHITE_WOOL,      
                Material.LIGHT_GRAY_WOOL, 
                Material.GRAY_WOOL,       
                Material.BLACK_WOOL       
        };

        int[] slots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
        for (int i = 0; i < materials.length; i++) {
            String colorName = getColorName(i);
            inventory.setItem(slots[i], buildItem(materials[i], colorName, ""));
        }

        inventory.setItem(31, buildItem(Material.BEACON, messages.colorCustomItem(),
                messages.colorCustomItemLore().toArray(new String[0])));

        inventory.setItem(36, buildItem(Material.ARROW, messages.colorBackItem(),
                messages.colorBackItemLore().toArray(new String[0])));

        inventory.setItem(40, buildItem(Material.BARRIER, messages.colorRemoveItem(),
                messages.colorRemoveItemLore().toArray(new String[0])));

        ItemStack bg = buildItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, bg);
            }
        }
    }

    private String getColorName(int index) {
        String[] colorNames = {
                "<red>Red", "<gold>Gold", "<yellow>Yellow", "<green>Green",
                "<dark_green>Dark Green", "<aqua>Aqua", "<dark_aqua>Dark Aqua",
                "<blue>Blue", "<dark_purple>Dark Purple", "<light_purple>Light Purple",
                "<white>White", "<gray>Gray", "<dark_gray>Dark Gray", "<black>Black"
        };
        return index < colorNames.length ? colorNames[index] : "<white>Color";
    }

    private ItemStack buildItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(MiniMessage.miniMessage().deserialize(name));
            if (loreLines.length > 0 && !loreLines[0].isEmpty()) {
                List<Component> lore = new java.util.ArrayList<>();
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

    public ColorService.ColorTarget getTarget() {
        return target;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}