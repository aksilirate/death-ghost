package com.elunar.plugin;

import org.bukkit.plugin.java.JavaPlugin;


public class DeathGhost extends JavaPlugin {

    public EventListener eventListener;
    public DataManager dataManager;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        dataManager = new DataManager(this);
        eventListener = new EventListener(this);

        if (!getDataFolder().exists()) {
            if (getDataFolder().mkdirs()) {
                getLogger().info("data dir was created.");
            }

        }
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
