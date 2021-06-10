package com.elunar.plugin;

import com.elunar.plugin.items.RandomRespawn;
import com.elunar.plugin.items.ResetLocation;
import com.elunar.plugin.items.RespawnHere;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.List;

import static org.bukkit.Bukkit.getServer;


public class EventListener implements Listener {

    public DeathGhost deathGhost;
    public DataManager dataManager;
    public RespawnHere respawnHere = new RespawnHere();
    public RandomRespawn randomRespawn = new RandomRespawn();
    public ResetLocation resetLocation = new ResetLocation();

    public EventListener(DeathGhost deathGhostClass) {
        this.deathGhost = deathGhostClass;
        this.dataManager = new DataManager(deathGhostClass);
    }


    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();


        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            dataManager.setYamlPlayerGhostMode(player.getName(), false);
            List<ItemStack> savedItems = dataManager.getYamlPlayerInventory(player.getName());
            ItemStack[] playerInventory = savedItems.toArray(new ItemStack[0]);
            player.getInventory().setContents(playerInventory);

            player.setInvulnerable(false);
            player.setInvisible(false);
            player.setAllowFlight(false);
            player.setFlying(false);

        } else {
            dataManager.setYamlPlayerInventory(player.getName(), player.getInventory().getContents());
            dataManager.setYamlPlayerGhostMode(player.getName(), true);
            dataManager.setYamlPlayerDeathLocation(player.getName(), player.getLocation());

            player.getInventory().clear();
            player.setInvulnerable(true);
            player.setInvisible(true);
            player.setAllowFlight(true);
            player.setFlying(true);
        }


    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            Location location = dataManager.getYamlPlayerDeathLocation(player.getName());
            event.getPlayer().getInventory().setHeldItemSlot(0);

            event.getPlayer().getInventory().setItem(0, respawnHere.getItem());
            event.getPlayer().getInventory().setItem(1, randomRespawn.getItem());
            event.getPlayer().getInventory().setItem(8, resetLocation.getItem());

            event.setRespawnLocation(location);
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            if (event.getItem() != null) {
                if (event.getItem().equals(resetLocation.getItem())) {
                    Location location = dataManager.getYamlPlayerDeathLocation(player.getName());
                    player.teleport(location);
                }
                if (event.getItem().equals(randomRespawn.getItem())) {
                    player.setHealth(0);
                }

            }
            event.setCancelled(true);
        }

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
    public void onEntityPickUp(EntityPickupItemEvent event) {
        Player player = (Player) event.getEntity();
        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            event.setCancelled(true);
        }
    }


    @SuppressWarnings("ConstantConditions")
    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent event) {
        System.out.println(event);
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

        deathGhost.eco = rsp.getProvider();
    }


    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getDamager();
        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            event.setCancelled(true);
        }

    }


    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {

        if (!(event instanceof Player)) {
            return;
        }

        Player player = (Player) event;
        if (dataManager.getYamlPlayerGhostMode(player.getName())) {
            event.setCancelled(true);
        }
    }


}
