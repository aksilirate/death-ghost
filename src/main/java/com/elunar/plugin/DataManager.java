package com.elunar.plugin;

import org.bukkit.Location;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class DataManager {

    public DeathGhost deathGhost;

    public DataManager(DeathGhost deathGhostClass) {
        this.deathGhost = deathGhostClass;
    }

    public void setYamlPlayerInventory(String playerName, ItemStack[] inventory) {
        File file = new File(deathGhost.getDataFolder(), playerName + ".yml");
        YamlConfiguration yaml_file = YamlConfiguration.loadConfiguration(file);

        for (int i = 0; i < inventory.length; i++){
            ItemStack item = inventory[i];
            if (item == null){
                yaml_file.set("inventory." + i, "empty");
            }else{
                yaml_file.set("inventory." + i, item);
            }

        }




        saveYamlFile(file, yaml_file);
    }

    @SuppressWarnings({"unused", "rawtypes"})
    public List<ItemStack> getYamlPlayerInventory(String playerName){
        File file = new File(deathGhost.getDataFolder(), playerName + ".yml");
        YamlConfiguration yaml_file = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection inventory = yaml_file.getConfigurationSection("inventory");

        if (inventory == null){
            return new ArrayList();
        }else{
            List<ItemStack> items = new ArrayList<>();
            for (String index: inventory.getKeys(false)){
                Object item = inventory.get(index);
                if(item instanceof ItemStack) items.add((ItemStack)item);
                else items.add(null);
            }
            return items;
        }


    }

    public void setYamlPlayerGhostMode(String playerName, boolean mode) {
        File file = new File(deathGhost.getDataFolder(), playerName + ".yml");
        YamlConfiguration yaml_file = YamlConfiguration.loadConfiguration(file);

        yaml_file.set("ghost_mode", mode);

        saveYamlFile(file, yaml_file);
    }


    @SuppressWarnings({"ConstantConditions", "unused"})
    public boolean getYamlPlayerGhostMode(String playerName) {
        File file = new File(deathGhost.getDataFolder(), playerName + ".yml");
        YamlConfiguration yaml_file = YamlConfiguration.loadConfiguration(file);

        if (!yaml_file.contains("ghost_mode")) {
            setYamlPlayerGhostMode(playerName, false);
            return false;
        } else {
            return (boolean) yaml_file.get("ghost_mode");

        }

    }


    public void setYamlPlayerDeathLocation(String playerName, Location location){
        File file = new File(deathGhost.getDataFolder(), playerName + ".yml");
        YamlConfiguration yaml_file = YamlConfiguration.loadConfiguration(file);

        yaml_file.set("death_location", location);

        saveYamlFile(file, yaml_file);
    }


    @SuppressWarnings("unused")
    public Location getYamlPlayerDeathLocation(String playerName) {
        File file = new File(deathGhost.getDataFolder(), playerName + ".yml");
        YamlConfiguration yaml_file = YamlConfiguration.loadConfiguration(file);

        if (!yaml_file.contains("death_location")) {
            Location defaultLocation = new Location(deathGhost.getServer().getWorld("world"), 0.0, 0.0, 0.0);
            setYamlPlayerDeathLocation(playerName, defaultLocation);
            return defaultLocation;
        } else {
            return (Location) yaml_file.get("death_location");

        }

    }





    public void saveYamlFile(File file, YamlConfiguration yaml_file) {
        try {
            yaml_file.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
