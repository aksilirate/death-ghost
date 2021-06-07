package com.elunar.plugin;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;


public class DataManager {

    private DeathGhost deathGhost;
    public DataManager(DeathGhost deathGhostClass){
        this.deathGhost = deathGhostClass;
    }

    public void setYamlPlayerInventory(String playerName, ItemStack[] items) {
        File file = new File(deathGhost.getDataFolder(), playerName + ".yml");
        YamlConfiguration yaml_file = YamlConfiguration.loadConfiguration(file);

        yaml_file.set("inventory", items);

        saveYamlFile(file, yaml_file);
    }


    public void saveYamlFile(File file, YamlConfiguration yaml_file) {
        try {
            yaml_file.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
