package com.elunar.plugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public class DeathGhost extends JavaPlugin {

    private static Economy eco = null;
    public EventListener eventListener;
    public DataManager dataManager;


    @Override
    public void onEnable() {
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
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
