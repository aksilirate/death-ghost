package com.elunar.plugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;


public class EventListener implements Listener {

    public DataManager dataManager;

    public EventListener(DeathGhost deathGhostClass){
        this.dataManager = new DataManager(deathGhostClass);
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        dataManager.setYamlPlayerInventory(player.getName(), player.getInventory().getContents());

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {

    }

}
