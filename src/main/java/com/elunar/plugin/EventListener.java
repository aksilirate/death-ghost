package com.elunar.plugin;

import com.elunar.plugin.items.RandomRespawn;
import com.elunar.plugin.items.ResetLocation;
import com.elunar.plugin.items.RespawnHere;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;


public class EventListener implements Listener {

    public DataManager dataManager;
    public RespawnHere respawnHere = new RespawnHere();
    public RandomRespawn randomRespawn = new RandomRespawn();
    public ResetLocation resetLocation = new ResetLocation();

    public EventListener(DeathGhost deathGhostClass) {
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
        event.getPlayer().getInventory().setHeldItemSlot(0);

        event.getPlayer().getInventory().setItem(0, respawnHere.getItem());
        event.getPlayer().getInventory().setItem(1, randomRespawn.getItem());
        event.getPlayer().getInventory().setItem(8, resetLocation.getItem());

        event.setRespawnLocation(location);

    }


    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getItemDrop().getItemStack().equals(respawnHere.getItem())) {
            event.setCancelled(true);
        } else if (event.getItemDrop().getItemStack().equals(randomRespawn.getItem())) {
            event.setCancelled(true);
        } else if (event.getItemDrop().getItemStack().equals(resetLocation.getItem())) {
            event.setCancelled(true);
        }

    }


    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (dataManager.getYamlPlayerGhostMode(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }


}
