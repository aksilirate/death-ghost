package com.elunar.plugin;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;


public class DeathGhost extends JavaPlugin {

    public Economy eco;
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
        return getServer().getPluginManager().getPlugin("Vault") != null;

    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
