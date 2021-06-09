package com.elunar.plugin.items;


import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;



public class RandomRespawn {
    public ItemStack getItem(){
        ItemStack itemStack = new ItemStack(Material.MAP);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("Random Respawn");
        itemMeta.addEnchant(Enchantment.LUCK, 1, false);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
