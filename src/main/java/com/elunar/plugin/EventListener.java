package com.elunar.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Locale;


public class EventListener implements Listener {

    public DataManager dataManager;

    public EventListener(DeathGhost deathGhostClass){
        this.dataManager = new DataManager(deathGhostClass);
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        dataManager.setYamlPlayerInventory(player.getName(), player.getInventory().getContents());
        dataManager.setYamlPlayerGhostMode(player.getName(), true);
        dataManager.setYamlPlayerDeathLocation(player.getName(), player.getLocation());



        player.getInventory().clear();
        player.setInvulnerable(true);
        player.setInvisible(true);
        player.setFlying(true);


    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = dataManager.getYamlPlayerDeathLocation(player.getName());
        player.teleport(location);

    }

}
