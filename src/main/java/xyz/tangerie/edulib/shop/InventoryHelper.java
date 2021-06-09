package xyz.tangerie.edulib.shop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InventoryHelper {
    public static ItemStack createGUIItem() {
        ItemStack item = new ItemStack(Material.ACACIA_BUTTON, 1);

        ItemMeta meta = item.getItemMeta();

        item.setItemMeta(meta);
        return item;
    }
}
