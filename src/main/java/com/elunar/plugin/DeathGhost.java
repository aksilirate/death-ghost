package com.elunar.plugin;

import com.elunar.plugin.tasks.DeathDistanceUpdater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;

import static org.bukkit.Bukkit.getServer;


public class DeathGhost extends JavaPlugin {

    public Economy eco;
    public EventListener eventListener;
    public DataManager dataManager;

    public ArrayList<Player> deadPlayers = new ArrayList<>();


    @Override
    public void onEnable() {
        if (!setupEconomy() ) {
            System.out.println("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (!getDataFolder().exists()) {
            if (getDataFolder().mkdirs()) {
                getLogger().info("Data dir was created.");
            }
        }

        dataManager = new DataManager(this);
        eventListener = new EventListener(this);


        @SuppressWarnings("unused") BukkitTask deathDistanceUpdater = new DeathDistanceUpdater(this).runTaskTimer(this, 20, 20);
        getServer().getPluginManager().registerEvents(new EventListener(this), this);


        RegisteredServiceProvider<Economy> eco_rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (eco_rsp != null){
            eco = eco_rsp.getProvider();
        }



    }


    private boolean setupEconomy() {
        return getServer().getPluginManager().getPlugin( "Vault") != null;
    }


    public int getRespawnHerePrice(Player player){
        String playerUuid = player.getUniqueId().toString();
        Location originalLocation = dataManager.getYamlPlayerDeathLocation(playerUuid);
        return (int) originalLocation.distance(player.getLocation()) + 1;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
