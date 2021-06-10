package com.elunar.plugin;

import com.elunar.plugin.tasks.DeathDistanceUpdater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;


public class DeathGhost extends JavaPlugin {

    public Economy eco;
    public EventListener eventListener;
    public DataManager dataManager;

    public ArrayList<Player> deadPlayers = new ArrayList<>();


    @Override
    public void onEnable() {
        BukkitTask deathDistanceUpdater = new DeathDistanceUpdater(this).runTaskTimer(this, 20, 20);
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        dataManager = new DataManager(this);
        eventListener = new EventListener(this);


        if (!setupEconomy() ) {
            System.out.println("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        if (!getDataFolder().exists()) {
            if (getDataFolder().mkdirs()) {
                getLogger().info("data dir was created.");
            }
        }


    }

    private boolean setupEconomy() {
        return getServer().getPluginManager().getPlugin( "Vault") != null;

    }


    public int getRespawnHerePrice(Player player){
        Location originalLocation = dataManager.getYamlPlayerDeathLocation(player.getName());
        return (int) originalLocation.distance(player.getLocation()) + 1;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
